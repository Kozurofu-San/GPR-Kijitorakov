#include "stm32f4xx.h"                  // Device header
#include "AD9910.h"
#include "functions.h"

// Pins
#define OSC_SPI SPI1
#define OSC_GPIO GPIOB
//#define OSC_P0	0	// OUT
//#define OSC_P1	1	// OUT
//#define OSC_P2	2	// OUT
//#define OSC_P3	3	// OUT
#define OSC_SDIO	5	// SPI MOSI
//#define OSC_SDO	6	// SPI MISO 
//#define OSC_PDC	15	// OUT
#define OSC_RST	6	// OUT
#define OSC_UP	8	// OUT
#define OSC_CS	9	// OUT
#define OSC_SCK	3	// SPI CLK

#define OSC_XTAL 40U	// MHz
#define OSC_FREQ_DESIRED 1000U	// MHz
//#define OSC_FREQ OSC_XTAL	// MHz
#define OSC_DIV (OSC_FREQ_DESIRED/OSC_XTAL)
#define OSC_FREQ (OSC_DIV*OSC_XTAL)	// MHz

static void oscil_write(char instr, char bytes, unsigned long long data);
static unsigned long long oscil_read(char instr, char bytes);
static void oscil_send(unsigned char data);
static unsigned char oscil_recieve(void);
static void oscil_turn(int arg);

#define READ 0x80
#define osc_en() (OSC_GPIO->BSRR = (1<<OSC_CS)<<16)
#define osc_dis() (OSC_GPIO->BSRR = (1<<OSC_CS)<<0)
#define osc_reset() {\
	OSC_GPIO->BSRR = (1<<OSC_RST)<<0;\
	delay_us(100);\
	OSC_GPIO->BSRR = (1<<OSC_RST)<<16;\
}
#define osc_update() {\
	OSC_GPIO->BSRR = (1<<OSC_UP)<<0;\
	delay_us(100);\
	OSC_GPIO->BSRR = (1<<OSC_UP)<<16;\
}
#define osc_on() {\
	SET_BIT(OSC_SPI->CR1,SPI_CR1_BIDIMODE|SPI_CR1_BIDIOE);\
	}
#define osc_off() {\
	CLEAR_BIT(OSC_SPI->CR1,SPI_CR1_BIDIMODE|SPI_CR1_BIDIOE);\
	}

struct osc_reg_struct{
	unsigned long long ftw: 32;
	unsigned long long pow: 16;
	unsigned long long asf: 14;
};

static struct osc_reg_struct osc_reg;

// Init
static const tGPIO_Line oscilIO[] = {
	{OSC_GPIO, OSC_SCK, GPIO_MODE_AF_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_NOPULL, GPIO_AF5_SPI2, 0},
	{OSC_GPIO, OSC_SDIO, GPIO_MODE_AF_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_NOPULL, GPIO_AF5_SPI2, 0},
	{OSC_GPIO, OSC_UP, GPIO_MODE_OUTPUT_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN, 0, 0},
	{OSC_GPIO, OSC_CS, GPIO_MODE_OUTPUT_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN,0,0},
	{OSC_GPIO, OSC_RST, GPIO_MODE_OUTPUT_PP, GPIO_SPEED_FREQ_VERY_HIGH, GPIO_PULLDOWN,0,0}
};
void ad9910_init_mcu(void){
	// GPIO
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOAEN);
	confIO(oscilIO);
	// SPI
	// AD9910 max CLK speed is 70 MHz
	// 8 bit, CPOL=0, CPHA=0, MBR, BR=42 MHz
	SET_BIT(RCC->APB2ENR,RCC_APB2ENR_SPI1EN);
	WRITE_REG(OSC_SPI->CR1,
		0*SPI_CR1_BIDIMODE|
		0*SPI_CR1_BIDIOE|
		SPI_CR1_SSM|
		SPI_CR1_SSI|
		SPI_CR1_MSTR|
		7U<<SPI_CR1_BR_Pos);
	SET_BIT(OSC_SPI->CR1,SPI_CR1_SPE);		// Turn on
}

void ad9910_deinit_mcu(void){
	CLEAR_BIT(OSC_SPI->CR1,SPI_CR1_SPE);		// Turn on
	CLEAR_BIT(RCC->APB2ENR,RCC_APB2ENR_SPI1EN);
}

// Oscillator initialization
void ad9910_init(void){
	struct AD9910_reg_struct registers;
	osc_on();
	long long reg;
	osc_update();
	osc_reset();
	osc_dis();
	osc_reg.asf = 0x3FFF;
	osc_reg.pow = 0;
	osc_reg.ftw = 0x33333333;
	
	oscil_write(AD9910_CFR1, 32,
		0U<<AD9910_RAM_enable|
		0U<<AD9910_RAM_playback_destination|
		0U<<AD9910_Manual_OSK_external_control|
		0U<<AD9910_Inverse_sinc_filter_enable|
		0U<<AD9910_Internal_profile_control|
		0U<<AD9910_Select_DDS_sine_output|
		0U<<AD9910_Load_LRR_IO_update|
		0U<<AD9910_Autoclear_digital_ramp_accumulator|
		0U<<AD9910_Autoclear_phase_accumulator|
		0U<<AD9910_Clear_digital_ramp_accumulator|
		0U<<AD9910_Clear_phase_accumulator|
		0U<<AD9910_Load_ARR_IO_update|
		0U<<AD9910_OSK_enable|
		0U<<AD9910_Select_auto_OSK|
		0U<<AD9910_Digital_power_down|
		0U<<AD9910_DAC_power_down|
		0U<<AD9910_REFCLK_input_power_down|
		0U<<AD9910_Aux_DAC_power_down|
		0U<<AD9910_External_power_down_control|
		0U<<AD9910_SDIO_inpit_only|
		0U<<AD9910_LSB_first);
	reg = oscil_read(AD9910_CFR1, 32);
	oscil_write(AD9910_CFR2, 32,
		1U<<AD9910_Enable_amplitude_scale_from_single_tone_profiles|
		0U<<AD9910_Internal_IO_update_active|
		0U<<AD9910_SYNC_CLK_enable|
		0U<<AD9910_Digital_ramp_destination|
		0U<<AD9910_Digital_ramp_enable|
		0U<<AD9910_Digital_ramp_no_dwell_high|
		0U<<AD9910_Digital_ramp_no_dwell_low|
		0U<<AD9910_Read_effective_FTW|
		0U<<AD9910_IO_update_rate_control|
		0U<<AD9910_PDCLK_enable|
		0U<<AD9910_PDCLK_invert|
		0U<<AD9910_TxEnable_invert|
		0U<<AD9910_Matched_latency_enable|
		0U<<AD9910_Data_assembler_hold_last_value|
		1U<<AD9910_Sync_timing_validation_disable|
		0U<<AD9910_Parallel_data_port_enable|
		0U<<AD9910_FM_gain);
	reg = oscil_read(AD9910_CFR2, 32);
	oscil_write(AD9910_CFR3, 32, 0*AD9910_CFR3_|
		3U<<AD9910_DRV0|
		5U<<AD9910_VCO_SEL|
		7U<<AD9910_I_CP|
		1U<<AD9910_REFCLK_input_divider_bypass|
		1U<<AD9910_REFCLK_input_divider_ResetB|
		0U<<AD9910_PFD_reset|
		1U<<AD9910_PLL_enable|
		OSC_DIV<<AD9910_N);
	reg = oscil_read(AD9910_CFR3, 32);
	oscil_write(AD9910_Auxiliary_DAC_Control, 32,
		255U<<AD9910_FSC);
	reg = oscil_read(AD9910_Auxiliary_DAC_Control, 32);
	osc_off();
}

static void oscil_write(char instr, char bytes, unsigned long long data){
	osc_en();
	oscil_send(instr);
	for(signed char i=bytes-8; i>=0; i-=8){
		oscil_send(data>>i);
	}
	delay_us(100);
	osc_dis();
	osc_update();
}

static unsigned long long oscil_read(char instr, char bytes){
	osc_en();
	long long data = 0;
	oscil_send(instr|READ);
	delay_us(10);
	for(signed char i=bytes-8; i>=0; i-=8){
		data <<= 8;
		data |= oscil_recieve();
	}
	osc_dis();
	return data;
}

static void oscil_send(unsigned char data){
	while((OSC_SPI->SR & SPI_SR_TXE)==0);
	OSC_SPI->DR = data;
	while((OSC_SPI->SR & SPI_SR_BSY)==1);
}

static unsigned char oscil_recieve(void){
	while((OSC_SPI->SR & SPI_SR_RXNE)==1);
	CLEAR_BIT(OSC_SPI->CR1,SPI_CR1_BIDIOE);
	while((OSC_SPI->SR & SPI_SR_RXNE)==0);
	SET_BIT(OSC_SPI->CR1,SPI_CR1_BIDIOE);
	char in_data = OSC_SPI->DR;
	return in_data;
}

// Turn on/off Oscillator
static void oscil_turn(int arg){
	osc_on();
	if(arg)
	oscil_write(AD9910_CFR1, 32,
		0U<<AD9910_RAM_enable|
		0U<<AD9910_RAM_playback_destination|
		0U<<AD9910_Manual_OSK_external_control|
		0U<<AD9910_Inverse_sinc_filter_enable|
		0U<<AD9910_Internal_profile_control|
		0U<<AD9910_Select_DDS_sine_output|
		0U<<AD9910_Load_LRR_IO_update|
		0U<<AD9910_Autoclear_digital_ramp_accumulator|
		0U<<AD9910_Autoclear_phase_accumulator|
		0U<<AD9910_Clear_digital_ramp_accumulator|
		0U<<AD9910_Clear_phase_accumulator|
		0U<<AD9910_Load_ARR_IO_update|
		0U<<AD9910_OSK_enable|
		0U<<AD9910_Select_auto_OSK|
		0U<<AD9910_Digital_power_down|
		0U<<AD9910_DAC_power_down|
		0U<<AD9910_REFCLK_input_power_down|
		0U<<AD9910_Aux_DAC_power_down|
		0U<<AD9910_External_power_down_control|
		0U<<AD9910_SDIO_inpit_only|
		0U<<AD9910_LSB_first);
	else
	oscil_write(AD9910_CFR1, 32,
		0U<<AD9910_RAM_enable|
		0U<<AD9910_RAM_playback_destination|
		0U<<AD9910_Manual_OSK_external_control|
		0U<<AD9910_Inverse_sinc_filter_enable|
		0U<<AD9910_Internal_profile_control|
		0U<<AD9910_Select_DDS_sine_output|
		0U<<AD9910_Load_LRR_IO_update|
		0U<<AD9910_Autoclear_digital_ramp_accumulator|
		0U<<AD9910_Autoclear_phase_accumulator|
		0U<<AD9910_Clear_digital_ramp_accumulator|
		0U<<AD9910_Clear_phase_accumulator|
		0U<<AD9910_Load_ARR_IO_update|
		0U<<AD9910_OSK_enable|
		0U<<AD9910_Select_auto_OSK|
		1U<<AD9910_Digital_power_down|
		1U<<AD9910_DAC_power_down|
		1U<<AD9910_REFCLK_input_power_down|
		0U<<AD9910_Aux_DAC_power_down|
		1U<<AD9910_External_power_down_control|
		0U<<AD9910_SDIO_inpit_only|
		0U<<AD9910_LSB_first);
	osc_off();
}

// Copy regs to RAM
void ad9910_power(short gain){
	osc_on();
	osc_reg.asf = gain;
	oscil_write(AD9910_Single_Tone_Profile+0, 64,
		(unsigned long long)gain<<AD9910_Amplitude_Scale_Factor|
		(unsigned long long)osc_reg.pow<<AD9910_Phase_Offset_Word|
		(unsigned long long)osc_reg.ftw<<AD9910_Frequency_Tuning_Word);
	osc_off();
}

// Set frequency
char ad9910_set_frequency(int f){
	osc_on();
	if(f>OSC_FREQ/2)
		return 0;
	unsigned long long ftw = (unsigned long long)f<<32;
	ftw = FDIVI(ftw, OSC_FREQ);
	osc_reg.ftw = ftw;
//	for(char i=0; i<8; i++)
	oscil_write(AD9910_Single_Tone_Profile+0, 64,
		(unsigned long long)osc_reg.asf<<AD9910_Amplitude_Scale_Factor|
		(unsigned long long)osc_reg.pow<<AD9910_Phase_Offset_Word|
		(unsigned long long)osc_reg.ftw<<AD9910_Frequency_Tuning_Word);
//	ftw = oscil_read(AD9910_Single_Tone_Profile+0, 64);
	osc_off();
	return 1;
}