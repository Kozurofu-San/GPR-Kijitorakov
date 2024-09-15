// VCO without dividers.
// 9V
// 515..1150 MHZ
#include "stm32f4xx.h"
#include "VCO.h"
#include "functions.h"

#define F_START 510
#define F_STOP 1150
#define SLOPE (FDIVI(0x0FFF0000,F_STOP-32-F_START))

#define CONTROL 4	// PA4
void vco_init(){
	// GPIO
	RCC->AHB1ENR |= RCC_AHB1ENR_GPIOAEN;
	GPIOA->MODER &= ~(3U<<CONTROL*2);
	GPIOA->MODER |=
		GPIO_MODE_ANALOG<<CONTROL*2;
	GPIOA->PUPDR &= ~(3U<<CONTROL*2);
	GPIOA->PUPDR |= 
		GPIO_NOPULL<<CONTROL*2;
	GPIOA->OSPEEDR &= ~(3U<<CONTROL*2);
	GPIOA->OSPEEDR |= 
		GPIO_SPEED_FREQ_VERY_HIGH<<CONTROL*2;
	// DAC
	RCC->APB1ENR |= RCC_APB1ENR_DACEN;
	DAC->CR |= DAC_CR_EN1;
	DAC->DHR12R1 = 0;
}

void vco_frequency(int f){
	f -= F_START-12;
	f *= SLOPE;
	f >>= 16;
	DAC->DHR12R1 = f;
}