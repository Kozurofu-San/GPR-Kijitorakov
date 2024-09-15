// =============== ADF4351 =======================
#include "stm32f4xx.h"                  // Device header
#include "ADF4351.h"
#include "functions.h"

// Pins
#define OSC_SPI SPI1
#define OSC_GPIO GPIOB
#define OSC_SCK 3		// SPI SCK, CLK
#define OSC_SDI	5		// SPI MOSI, DATA
#define OSC_CS 9		// OUT, CE
#define OSC_UP 8		// OUT, LE
#define OSC_PDR 7		// OUT
#define OSC_MUX 6		// INPUT
//#define OSC_LD 11		// INPUT

static void oscil_write(int reg);
#define osc_pwr_en() (OSC_GPIO->BSRR = (1<<OSC_PDR)<<0)
#define osc_pwr_dis() (OSC_GPIO->BSRR = (1<<OSC_PDR)<<16)
#define osc_read_lock() ((OSC_GPIO->IDR&(1<<OSC_MUX))==(1<<OSC_MUX))

#define osc_en() (OSC_GPIO->BSRR = (1<<OSC_CS)<<16)
#define osc_dis() (OSC_GPIO->BSRR = (1<<OSC_CS)<<0)

#define osc_update() {\
	OSC_GPIO->BSRR = (1<<OSC_UP)<<0;\
	__NOP();\
	__NOP();\
	__NOP();\
	__NOP();\
	__NOP();\
	__NOP();\
	__NOP();\
	__NOP();\
	__NOP();\
	OSC_GPIO->BSRR = (1<<OSC_UP)<<16;\
}

#define OSC_XTAL 25	// MHz
#define RDIV 25
#define FPD (OSC_XTAL/RDIV)	// Step
#define MOD 2U
#define FastLockTimerValue ((20+20)*FPD)/MOD
#define PFD (1000/500)

struct osc_reg_struct{
	unsigned int intg: 16;
	unsigned int divsel: 3;
	unsigned int gain: 2;
	unsigned int presc: 1;
	unsigned int fdbk: 1;
};

static struct osc_reg_struct osc_reg;
struct ADF4351_reg_struct registers;

// Init
static const tGPIO_Line oscilIO[] = {
	{OSC_GPIO,OSC_SCK, GPIO_MODE_AF_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN, GPIO_AF5_SPI2, 0},
	{OSC_GPIO,OSC_SDI, GPIO_MODE_AF_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN, GPIO_AF5_SPI2, 0},
	{OSC_GPIO,OSC_CS, GPIO_MODE_OUTPUT_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN,0,0},
	{OSC_GPIO,OSC_UP, GPIO_MODE_OUTPUT_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN,0,0},
	{OSC_GPIO,OSC_PDR, GPIO_MODE_OUTPUT_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN,0,0},
	{OSC_GPIO,OSC_MUX, GPIO_MODE_INPUT, 0, GPIO_PULLUP,0,0}
};
void adf4351_init_mcu(void){
	// GPIO
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOBEN);
	configureIO(oscilIO, sizeof(oscilIO)/sizeof(tGPIO_Line));
	// SPI
	// ADF4351 max CLK speed is 20 MHz
	// 8 bit, CPOL=0,CPHA=0, MBR, BR=84/8(2)=10.5 MHz
	if(!(OSC_SPI->CR1 & SPI_CR1_SPE)){
	CLEAR_BIT(OSC_SPI->CR1,SPI_CR1_SPE);		// Turn off
	SET_BIT(RCC->APB2ENR,RCC_APB2ENR_SPI1EN);
	WRITE_REG(OSC_SPI->CR1,
		SPI_CR1_SSM|
		SPI_CR1_SSI|
		SPI_CR1_MSTR|
		2U<<SPI_CR1_BR_Pos);
	SET_BIT(OSC_SPI->CR1,SPI_CR1_SPE);		// Turn on
	}
}
void adf4351_deinit_mcu(void){
	// GPIO
	deconfigureIO(oscilIO, sizeof(oscilIO)/sizeof(tGPIO_Line));
	// SPI
	CLEAR_BIT(OSC_SPI->CR1,SPI_CR1_SPE);		// Turn off
	CLEAR_BIT(RCC->APB2ENR,RCC_APB2ENR_SPI1EN);
}

// Oscillator initialization
void adf4351_init(void){
	osc_reg.intg = 128;
	osc_reg.presc = 1;
	osc_reg.gain = 0;
	osc_reg.divsel = 5;
	osc_reg.fdbk = 1;
	
	int *ptr = (int*)&registers;
	*ptr++ = 0x06400000;
	*ptr++ = 0x00008011;
	*ptr++ = 0x18066FC2;
	*ptr++ = 0x00C484B3;
	*ptr++ = 0x00D02024;
	*ptr++ = 0x00580005;
	
	
	oscil_write(5|bR5|
		1U<<ADF4351_LDPINMOD);
	oscil_write(4|
		osc_reg.fdbk<<ADF4351_FDBKSEL|
		osc_reg.divsel<<ADF4351_RFDIVSEL|
		2<<ADF4351_BNDSELCLKDIV|
		0U<<ADF4351_VCOPD|
		0U<<ADF4351_MTLD|
		0U<<ADF4351_AUXOUTSEL|
		0U<<ADF4351_AUXOUTEN|
		0U<<ADF4351_AUXOUTPOW|
		1U<<ADF4351_RFOUTEN|
		osc_reg.gain<<ADF4351_OUTPOW);
	oscil_write(3|
		1U<<ADF4351_BNDSEL|
		1U<<ADF4351_ABP|
		1U<<ADF4351_CHRGCNCL|
		1U<<ADF4351_CSR|
		0U<<ADF4351_DIVMOD|
		500U<<ADF4351_CLKDIV);
	oscil_write(2|
		0U<<ADF4351_LNMOD|
		6U<<ADF4351_MUXOUT|
		0U<<ADF4351_REFDBLR|
		0U<<ADF4351_RDIV2|
		RDIV<<ADF4351_RCNT|
		1U<<ADF4351_DBLBUF|
		7U<<ADF4351_CPCURSET|
		1U<<ADF4351_LDF|
		1U<<ADF4351_LDP|
		1U<<ADF4351_PDPOL|
		0U<<ADF4351_POWDOWN|
		0U<<ADF4351_CP3ST|
		0U<<ADF4351_CNTRES);
	oscil_write(1|
		0U<<ADF4351_PHADJ|
		0U<<ADF4351_PRESC|
		1U<<ADF4351_PHASE|
		MOD<<ADF4351_MOD);
	oscil_write(0|
		3200U<<ADF4351_INT|
		0U<<ADF4351_FRAC);
	// Fast lock
	oscil_write(3|
		1U<<ADF4351_BNDSEL|
		1U<<ADF4351_ABP|
		1U<<ADF4351_CHRGCNCL|
		0U<<ADF4351_CSR|
		1U<<ADF4351_DIVMOD|
		2000<<ADF4351_CLKDIV);
	oscil_write(0|
		3200U<<ADF4351_INT|
		0U<<ADF4351_FRAC);
		
	osc_pwr_dis();
}

// Write via SPI
static void oscil_write(int reg){
	osc_en();
	for(signed char i=24; i>=0; i-=8){
		while(!(OSC_SPI->SR & SPI_SR_TXE));
		OSC_SPI->DR = reg>>i;
	}
	while((OSC_SPI->SR & SPI_SR_BSY));
	osc_dis();
	osc_update();
}

// Set frequency 35..4400 MHz
char adf4351_set_frequency(int f){
	char div, logdiv;	// e - error
	if(f<35||f>4400)
		return -1;
	if(f>3600)
		osc_reg.presc = 1;	// Prescaler = 4/5: N MIN = 23
	else
		osc_reg.presc = 0;	// Prescaler = 8/9: N MIN = 75
	
	osc_reg.fdbk = (f<=2200) ? 1 : 0;
		
	div = 1;
	logdiv = 0;
	for(int i=f*2; i<=4400; i*=2){
		logdiv++;
		div *= 2;
	}
	osc_reg.divsel = logdiv;
	osc_reg.intg = f*div/FPD;
	osc_reg.presc = (osc_reg.intg<3600)?0:1;
	
	oscil_write(4|
		osc_reg.fdbk<<ADF4351_FDBKSEL|
		osc_reg.divsel<<ADF4351_RFDIVSEL|
		2U<<ADF4351_BNDSELCLKDIV|
		0U<<ADF4351_VCOPD|
		0U<<ADF4351_MTLD|
		0U<<ADF4351_AUXOUTSEL|
		0U<<ADF4351_AUXOUTEN|
		0U<<ADF4351_AUXOUTPOW|
		1U<<ADF4351_RFOUTEN|
		osc_reg.gain<<ADF4351_OUTPOW);
	oscil_write(1|
		0U<<ADF4351_PHADJ|
		osc_reg.presc<<ADF4351_PRESC|
		1U<<ADF4351_PHASE|
		2U<<ADF4351_MOD);
	oscil_write(0|
		osc_reg.intg<<ADF4351_INT|
		0U<<ADF4351_FRAC);
	return 1;
}

// Set gain 0..3, turn off -1
void adf4351_set_power(short gain){
	osc_reg.gain = gain;
	if(gain >= 0){
	osc_pwr_en();
	oscil_write(4|
		osc_reg.fdbk<<ADF4351_FDBKSEL|
		osc_reg.divsel<<ADF4351_RFDIVSEL|
		2U<<ADF4351_BNDSELCLKDIV|
		0U<<ADF4351_VCOPD|
		0U<<ADF4351_MTLD|
		0U<<ADF4351_AUXOUTSEL|
		0U<<ADF4351_AUXOUTEN|
		0U<<ADF4351_AUXOUTPOW|
		1U<<ADF4351_RFOUTEN|
		osc_reg.gain<<ADF4351_OUTPOW);
	oscil_write(0|
		osc_reg.intg<<ADF4351_INT|
		0U<<ADF4351_FRAC);
	} else{
		osc_pwr_dis();
	}
}

inline char adf4351_read_lock(){
	return (OSC_GPIO->IDR&(1<<OSC_MUX))==(1<<OSC_MUX);
}