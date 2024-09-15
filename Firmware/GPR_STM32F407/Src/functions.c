// Board F4VE
// STM32F407VET6
// 168 MHz
// FLASH 512K
// RAM 196K
// Cortex-M4
#include "stm32f4xx.h"                  // Device header
#include "functions.h"
#include "FreeRTOS.h"

void configureIO(const tGPIO_Line *Line, uint8_t len){
	char bsrr_shift;
	char af_shift;
	for(unsigned char i=0; i<len; i++){
		if(Line[i].STATE)
			bsrr_shift = 0;
		else
			bsrr_shift = 16;
		if(Line[i].PIN<8)
			af_shift = 0;
		else
			af_shift = 8;
		WRITE_REG(Line[i].GPIOx->BSRR,(1U<<Line[i].PIN)<<bsrr_shift);
		MODIFY_REG(Line[i].GPIOx->MODER,3U<<Line[i].PIN*2,
			(Line[i].MODE&0xF)<<Line[i].PIN*2);
		MODIFY_REG(Line[i].GPIOx->OTYPER,1U<<Line[i].PIN,
			(Line[i].MODE>>4)<<Line[i].PIN);
		MODIFY_REG(Line[i].GPIOx->OSPEEDR,3U<<Line[i].PIN*2,
			Line[i].SPEED<<Line[i].PIN*2);
		MODIFY_REG(Line[i].GPIOx->PUPDR,3U<<Line[i].PIN*2,
			Line[i].PULL<<Line[i].PIN*2);
		MODIFY_REG(Line[i].GPIOx->AFR[af_shift/8],15U<<(Line[i].PIN-af_shift)*4,
			(unsigned)Line[i].AF<<(Line[i].PIN-af_shift)*4);
	}
}

void deconfigureIO(const tGPIO_Line *Line, uint8_t len){
	char bsrr_shift;
	char af_shift;
	for(unsigned char i=0; i<len; i++){
		if(Line[i].STATE)
			bsrr_shift = 0;
		else
			bsrr_shift = 16;
		if(Line[i].PIN<8)
			af_shift = 0;
		else
			af_shift = 8;
		WRITE_REG(Line[i].GPIOx->BSRR,(1U<<Line[i].PIN)<<bsrr_shift);
		MODIFY_REG(Line[i].GPIOx->MODER,3U<<Line[i].PIN*2,
			(GPIO_MODE_INPUT&0xF)<<Line[i].PIN*2);
		MODIFY_REG(Line[i].GPIOx->OTYPER,1U<<Line[i].PIN,
			(GPIO_MODE_INPUT>>4)<<Line[i].PIN);
		MODIFY_REG(Line[i].GPIOx->OSPEEDR,3U<<Line[i].PIN*2,
			GPIO_SPEED_FREQ_LOW<<Line[i].PIN*2);
		MODIFY_REG(Line[i].GPIOx->PUPDR,3U<<Line[i].PIN*2,
			GPIO_NOPULL<<Line[i].PIN*2);
		MODIFY_REG(Line[i].GPIOx->AFR[af_shift/8],15U<<(Line[i].PIN-af_shift)*4,
			0<<(Line[i].PIN-0)*4);
	}
}

long long bin2bcd(int bin){
	long long bcd = 0;
	char cnt = 32;
	for(;cnt--;){
		bcd |= (bin%10)<<(32-4);
		bin /= 10;
		bcd >>= 4;
	}
	return bcd;
}

void even_odd_to_half(volatile short p[], uint32_t len){
	short t;
	for(int i=1; i<len/2; i++){
		t = p[i];
		for(int j=i+1; j<len-1; j++)
			p[j-1] = p[j];
		p[len-2] = t;
	}
}

void debug_delay_init(void){
	CoreDebug->DEMCR |= CoreDebug_DEMCR_TRCENA_Msk;
	DWT->CYCCNT = 0;
	DWT->CTRL |= DWT_CTRL_CYCCNTENA_Msk; 
}
void debug_delay_us(unsigned int us){
	us *= (SystemCoreClock/1000000);
	DWT->CYCCNT = 0;
	DWT->CTRL |= DWT_CTRL_CYCCNTENA_Msk; 
	while(DWT->CYCCNT < us);
	DWT->CTRL &= ~DWT_CTRL_CYCCNTENA_Msk;
}

void delay_init(void){
	RCC->APB1ENR |= RCC_APB1ENR_TIM6EN;	// APB1 42 MHz
	TIM6->CR1 = TIM_CR1_OPM;
	TIM6->PSC = 2*42-1;	// 1 us
	TIM6->SR = 0;
}
void delay_us(unsigned int us){
	TIM6->ARR = us;
	TIM6->CR1 |= TIM_CR1_CEN;
	while((TIM6->SR & TIM_SR_UIF)==0);
	TIM6->SR = 0;
	TIM6->CR1 &= ~TIM_CR1_CEN;
}


void timer_init(int ms){
	RCC->APB1ENR |= RCC_APB1ENR_TIM7EN;	// APB1 42 MHz
	TIM7->CR1 = TIM_CR1_ARPE;
	TIM7->DIER = TIM_DIER_UIE;
	TIM7->PSC = (2*42-1)*100;	// 0.1 ms
	TIM7->ARR = 10*ms;
	TIM7->SR = 0;
	NVIC_EnableIRQ(TIM7_IRQn);
	NVIC_SetPriority(TIM7_IRQn, configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY+1);
	TIM7->CR1 |= TIM_CR1_CEN;
}

void TIM7_IRQHandler(){
	timer_callback();
	TIM7->SR = 0;
}

// LED on (F4VE)
#define LED2 6	// PA6
#define LED3 7	// PA7
void led_init(void){
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOAEN);
	SET_BIT(GPIOA->BSRR,(1U<<LED2)<<16|(1U<<LED3)<<16);
	MODIFY_REG(GPIOA->MODER,3U<<LED2*2|3U<<LED3*2,
		(GPIO_MODE_OUTPUT_PP&0xF)<<LED2*2|
		(GPIO_MODE_OUTPUT_PP&0xF)<<LED3*2);
	MODIFY_REG(GPIOA->OTYPER,1U<<LED2|1U<<LED3,
		(GPIO_MODE_OUTPUT_PP>>4)<<LED2|
		(GPIO_MODE_OUTPUT_PP>>4)<<LED3);
	MODIFY_REG(GPIOA->OSPEEDR,3U<<LED2*2|3U<<LED3*2,
		GPIO_SPEED_FREQ_VERY_HIGH<<LED2*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<LED3*2);
	MODIFY_REG(GPIOA->PUPDR,3U<<LED2*2|3U<<LED3*2,
		GPIO_PULLUP<<LED2*2|
		GPIO_PULLUP<<LED3*2);
}
// Turn on/off LED
// a1.0=1 - turn on
// a1.0=0 - turn off
void led_turn(char n,int arg){
	unsigned int pin = (1U<<n)<<16;	// On
	if((arg&1)==0)
		pin >>= 16;			// Off
	GPIOA->BSRR = pin;
}

// Buttons
#define BUT_GPIO GPIOE
#define BUT0 4	// PE4
#define BUT1 3	// PE3
void button_init(void){
	// GPIO
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOEEN);
	WRITE_REG(GPIOE->BSRR,(1U<<BUT0|1U<<BUT1)<<16);
	MODIFY_REG(GPIOE->MODER,3U<<BUT0*2|3U<<BUT1*2,
		GPIO_MODE_INPUT<<BUT0*2|
		GPIO_MODE_INPUT<<BUT1*2);
	MODIFY_REG(GPIOE->PUPDR,3U<<BUT0*2|3U<<BUT1*2,
		GPIO_PULLUP<<BUT0*2|
		GPIO_PULLUP<<BUT1*2);
	// EXTI
	SYSCFG->EXTICR[0] |= SYSCFG_EXTICR1_EXTI3_PE;
	SYSCFG->EXTICR[1] |= SYSCFG_EXTICR2_EXTI4_PE;
	SET_BIT(EXTI->IMR,1U<<BUT0|1U<<BUT1);
	SET_BIT(EXTI->FTSR,1U<<BUT0|1U<<BUT1);
	SET_BIT(EXTI->RTSR,1U<<BUT0|1U<<BUT1);
	WRITE_REG(EXTI->PR,1U<<BUT0|1U<<BUT1);
	NVIC_SetPriority(EXTI4_IRQn, configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY+1);
	NVIC_EnableIRQ(EXTI4_IRQn);
	NVIC_SetPriority(EXTI3_IRQn, configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY+1);
	NVIC_EnableIRQ(EXTI3_IRQn);
}

// Button interrupts
void EXTI4_IRQHandler(void){	// K0 button
	if(!(BUT_GPIO->IDR & 1<<BUT0))
		button0_press_callback();
	if((BUT_GPIO->IDR & 1<<BUT0))
		button0_release_callback();
	EXTI->PR = 1U<<BUT0;
}

//extern volatile char touch_calibrate_flag;
void EXTI3_IRQHandler(void){	// K1 button
	if(!(BUT_GPIO->IDR & 1<<BUT1))
		button1_press_callback();
	if((BUT_GPIO->IDR & 1<<BUT1))
		button1_release_callback();
	EXTI->PR = 1U<<BUT1;
}

// Sleep mode
void sleep_init(void){
	DBGMCU->CR|=DBGMCU_CR_DBG_SLEEP;
	DBGMCU->CR|=DBGMCU_CR_DBG_STOP;
	DBGMCU->CR|=DBGMCU_CR_DBG_STANDBY;
	
	RCC->APB1ENR|=RCC_APB1ENR_PWREN;
	SCB->SCR|=SCB_SCR_SLEEPDEEP_Msk;
	PWR->CR&=~PWR_CR_PDDS;
	PWR->CR|=PWR_CR_CWUF;
	PWR->CSR|=PWR_CSR_EWUP;
}

// RTC
void rtc_init(struct rtc_struct *value){
	value->year = 0x21;
	value->month = 0x3;
	value->date = 0x05;
	value->week = 0x5;
	value->hour = 0x19;
	value->min = 0x53;
	value->sec = 0x00;
	RCC->APB1ENR |= RCC_APB1ENR_PWREN;
	PWR->CR |= PWR_CR_DBP;
	if(RTC->ISR&RTC_ISR_INITS) goto set;		// LSE
	RCC->BDCR |= RCC_BDCR_BDRST;
	RCC->BDCR &= ~RCC_BDCR_BDRST;
	RCC->BDCR |= RCC_BDCR_LSEON;  
	while (!(RCC->BDCR&RCC_BDCR_LSERDY)){} 
	RCC->BDCR |= 1<<RCC_BDCR_RTCSEL_Pos;
	
//	RCC->CSR |= RCC_CSR_LSION;
//	while(!(RCC->CSR&RCC_CSR_LSIRDY)){}
//	RCC->APB1ENR |= RCC_APB1ENR_PWREN;
//	PWR->CR |= PWR_CR_DBP;
//	RCC->BDCR |= 2<<RCC_BDCR_RTCSEL_Pos;		// LSI
	
	
	RTC->WPR = 0xCA;	//Open access to RTC
	RTC->WPR = 0x53;	//Open access to RTC
	RTC->CR &= ~RTC_CR_ALRAE;
	while(!(RTC->ISR & RTC_ISR_ALRAWF)){}	
	RTC->ALRMAR |= RTC_ALRMAR_MSK1 | RTC_ALRMAR_MSK2 | RTC_ALRMAR_MSK3 | RTC_ALRMAR_MSK4;	//Alarm every 1 second
	RTC->ALRMAR |= RTC_ALRMAR_SU_0;	// 1 second in BCD format	
	RTC->CR |= RTC_CR_ALRAE;
	RTC->CR |= RTC_CR_ALRAIE;
	RTC->ALRMBR |= RTC_ALRMBR_MSK1 | RTC_ALRMBR_MSK2 | RTC_ALRMBR_MSK3 | RTC_ALRMBR_MSK4;	//Alarm every 1 day
	RTC->ALRMBR |= RTC_ALRMBR_DU_0;	// 1 day in BCD format	
	RTC->CR |= RTC_CR_ALRBE;
	RTC->CR |= RTC_CR_ALRBIE;
	RTC->WPR = 0xFF;	//Close access to RTC
		
	RCC->BDCR |= RCC_BDCR_RTCEN;
	rtc_update(value);
		
	set:
	// EXTI17 - RTC IRQ
	EXTI->PR = EXTI_PR_PR17;
	EXTI->IMR |= EXTI_IMR_MR17;
	EXTI->EMR &= ~EXTI_EMR_MR17;
	EXTI->RTSR |= EXTI_RTSR_TR17;	
	NVIC_SetPriority(RTC_Alarm_IRQn, 40);
	NVIC_EnableIRQ(RTC_Alarm_IRQn);
	RTC->ISR &= ~RTC_ISR_ALRAF;
	RTC->ISR &= ~RTC_ISR_ALRBF;
}

void rtc_update(struct rtc_struct *value){
	RTC->WPR = 0xCA;	// Read protection
	RTC->WPR = 0x53;
	RTC->ISR |= RTC_ISR_INIT;
	while(!(RTC->ISR & RTC_ISR_INITF)){};
	RTC->PRER = 99<<16;
	RTC->PRER |= 399;
	RTC->TR = value->hour << RTC_TR_HU_Pos|
		value->min << RTC_TR_MNU_Pos|
		value->sec << RTC_TR_SU_Pos;
	RTC->DR = value->year << RTC_DR_YU_Pos|
		value->week << RTC_DR_WDU_Pos|
		value->month << RTC_DR_MU_Pos|
		value->date << RTC_DR_DU_Pos;
	RTC->ISR &= ~RTC_ISR_INIT;
	RTC->WPR = 0xFF;
}

//extern void RTC_Alarm_IRQHandler(void){
//	EXTI->PR = EXTI_PR_PR17;
//	if (RTC->ISR & RTC_ISR_ALRAF){
//		RTC->ISR &= ~RTC_ISR_ALRAF;
//		time_update();
////		flags.time_update = 1;
//	}
//	if (RTC->ISR & RTC_ISR_ALRBF){
//		RTC->ISR &= ~RTC_ISR_ALRBF;
//		date_update();
////		flags.date_update = 1;
//	}
//}

//void timer_base_init(void){
//	RCC->APB1ENR |= RCC_APB1ENR_TIM7EN;
//	TIM7->PSC = 16800-1;
//	TIM7->ARR = 10000;
//	TIM7->DIER |= TIM_DIER_UIE;
//	TIM7->CR1 |= TIM_CR1_CEN;
//	NVIC_SetPriority(TIM7_IRQn,0);
//	NVIC_ClearPendingIRQ(TIM7_IRQn);
//	NVIC_EnableIRQ(TIM7_IRQn);
//}

//void TIM7_IRQHandler(void){
//	timer_base_flag = 1;
//}

//#define DAC_OUT1 4	// PA4
//#define DAC_OUT2 5	// PA5
//void dac_init(void){
//	// GPIO
//	RCC->AHB1ENR |= RCC_AHB1ENR_GPIOAEN;
//	GPIOA->MODER &= ~(3U<<DAC_OUT1*2|3U<<DAC_OUT2*2);
//	GPIOA->MODER |=
//		GPIO_MODE_ANALOG<<DAC_OUT1*2|
//		GPIO_MODE_ANALOG<<DAC_OUT2*2;
//	GPIOA->PUPDR &= ~(3U<<DAC_OUT1*2|3U<<DAC_OUT2*2);
//	GPIOA->PUPDR |= 
//		GPIO_NOPULL<<DAC_OUT1*2|
//		GPIO_NOPULL<<DAC_OUT2*2;
//	GPIOA->OSPEEDR &= ~(3U<<DAC_OUT1*2|3U<<DAC_OUT2*2);
//	GPIOA->OSPEEDR |= 
//		GPIO_SPEED_FREQ_VERY_HIGH<<DAC_OUT1*2|
//		GPIO_SPEED_FREQ_VERY_HIGH<<DAC_OUT2*2;
//	// DAC
//	RCC->APB1ENR |= RCC_APB1ENR_DACEN;
//	DAC->CR |= DAC_CR_EN1|DAC_CR_EN2;
//	DAC->DHR12R1 |= 1000;
//	DAC->DHR12R1 |= 2000;
//}
