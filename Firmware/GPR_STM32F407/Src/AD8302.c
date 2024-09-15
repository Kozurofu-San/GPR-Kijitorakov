//================== AD8302 ===============
#include "stm32f4xx.h"
#include "AD8302.h"
#include "functions.h"

#define MIX_GPIO GPIOA
#define VPHS 2	// PA2
#define VMAG 1	// PA1
//#define VREF REF+

const tGPIO_Line mixerIO[] = {
	{MIX_GPIO, VMAG, GPIO_MODE_ANALOG, 0, GPIO_NOPULL, 0, 0},
	{MIX_GPIO, VPHS, GPIO_MODE_ANALOG, 0, GPIO_NOPULL, 0, 0},
};
void ad8302_init_mcu(void){	// Init mcu for mixer
	// GPIO
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOAEN);
	configureIO(mixerIO, sizeof(mixerIO)/sizeof(tGPIO_Line));
	// ADC
	SET_BIT(RCC->APB2ENR,RCC_APB2ENR_ADC1EN);
	WRITE_REG(ADC1->JSQR,1U<<ADC_JSQR_JL_Pos|
		VPHS<<ADC_JSQR_JSQ3_Pos|
		VMAG<<ADC_JSQR_JSQ4_Pos);
	WRITE_REG(ADC1->JOFR1,-2048+68);
	WRITE_REG(ADC1->JOFR2,-2048+68); // 0.03V..1.8V, 0.03V -> 68bit, 1bit ->0.44mV
	MODIFY_REG(ADC1->SMPR2,7U<<VPHS*3|7U<<VMAG*3,
		1U<<VPHS*3|
		1U<<VMAG*3);		// 15 cycles
	SET_BIT(ADC1->CR1,ADC_CR1_SCAN);
	SET_BIT(ADC1->CR2,ADC_CR2_ADON);
}

static short cnt = 0;
short* ad8302_read(short *dst, char avg){
	char cnt = 1<<avg;
	short val1 = 0, val2 = 0;
	for(char i=0; i<cnt; i++){
		SET_BIT(ADC1->CR2, ADC_CR2_JSWSTART);
		while((ADC1->SR&ADC_SR_JEOC)==0);
		ADC1->SR = 0;
		val1 += ADC1->JDR1;
		val2 += ADC1->JDR2;
	}
	val1 >>= avg;
	val2 >>= avg;
	*dst = val1;
	dst++;
	*dst = val2;
	dst++;
	return dst;
}
