// XPT2046
#include "stm32f4xx.h"                  // Device header
#include "functions.h"
#include "XPT2046.h"
#include "W25Q.h"

#define xpt2046_en()	GPIOB->BSRR = (1<<T_CS)<<16
#define xpt2046_dis()	GPIOB->BSRR = (1<<T_CS)<<0

char xpt2046_send(char data);
short xpt2046_get(char address);
void calib_write(void);

int calib[4];
extern unsigned char flash_buffer[4096];
int calib_rom __attribute__((section(".ROM1.__at_0x00000000")));
int calib_rom2 __attribute__((section("ROM1")));
#define CALIB_ADDRESS 0
#define CALIB_LEN sizeof(calib)
	
void xpt2046_init_mcu(void){
	// GPIO - SPI, Chip select
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOBEN);
	WRITE_REG(GPIOB->BSRR,(1U<<T_CS|1U<<T_SCK|1U<<T_MISO|1U<<T_MOSI)<<16);
	MODIFY_REG(GPIOB->MODER,3U<<T_CS*2|3U<<T_SCK*2|3U<<T_MISO*2|3U<<T_MOSI*2,
		(GPIO_MODE_OUTPUT_PP&0xF)<<T_CS*2|
		(GPIO_MODE_AF_PP&0xF)<<T_SCK*2|
		(GPIO_MODE_AF_PP&0xF)<<T_MISO*2|
		(GPIO_MODE_AF_PP&0xF)<<T_MOSI*2);
	MODIFY_REG(GPIOB->OTYPER,1U<<T_CS|1U<<T_SCK|1U<<T_MISO|1U<<T_MOSI,
		(GPIO_MODE_OUTPUT_PP>>4)<<T_CS|
		(GPIO_MODE_AF_PP>>4)<<T_SCK|
		(GPIO_MODE_AF_PP>>4)<<T_MISO|
		(GPIO_MODE_AF_PP>>4)<<T_MOSI);
	MODIFY_REG(GPIOB->OSPEEDR,3U<<T_CS*2|3U<<T_SCK*2|3U<<T_MISO*2|3U<<T_MOSI*2,
		GPIO_SPEED_FREQ_VERY_HIGH<<T_CS*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<T_SCK*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<T_MISO*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<T_MOSI*2);
	MODIFY_REG(GPIOB->PUPDR,3U<<T_CS*2|3U<<T_SCK*2|3U<<T_MISO*2|3U<<T_MOSI*2,
		GPIO_PULLUP<<T_CS*2|
		GPIO_PULLDOWN<<T_SCK*2|
		GPIO_PULLDOWN<<T_MISO*2|
		GPIO_PULLDOWN<<T_MOSI*2);
	MODIFY_REG(GPIOB->AFR[1],15U<<(T_SCK-8)*4|15U<<(T_MISO-8)*4|15U<<(T_MOSI-8)*4,
		(unsigned)GPIO_AF5_SPI2<<(T_SCK-8)*4|
		(unsigned)GPIO_AF5_SPI2<<(T_MISO-8)*4|
		(unsigned)GPIO_AF5_SPI2<<(T_MOSI-8)*4);
	// SPI - touch, 2.5 MHz max
	if(!(SPI2->CR1 & SPI_CR1_SPE)){
	CLEAR_BIT(SPI2->CR1,SPI_CR1_SPE);
	SET_BIT(RCC->APB1ENR,RCC_APB1ENR_SPI2EN);
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOCEN);
	WRITE_REG(SPI2->CR1,5U<<SPI_CR1_BR_Pos|
		SPI_CR1_SSM|SPI_CR1_SSI|SPI_CR1_MSTR);
	SET_BIT(SPI2->CR1,SPI_CR1_SPE);		// Turn on
	}
	// Touch PEN
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOCEN);
	WRITE_REG(GPIOC->BSRR,(1U<<T_PEN)<<16);
	MODIFY_REG(GPIOC->MODER,3U<<T_PEN*2,GPIO_MODE_INPUT<<T_PEN*2);
	MODIFY_REG(GPIOC->PUPDR,3U<<T_PEN*2,GPIO_PULLDOWN<<T_PEN*2);
//	SYSCFG->EXTICR[1] |= SYSCFG_EXTICR2_EXTI5_PC;
//	SET_BIT(EXTI->IMR,1U<<T_PEN);
//	SET_BIT(EXTI->FTSR,1U<<T_PEN);
////	SET_BIT(EXTI->RTSR,1U<<T_PEN);
//	WRITE_REG(EXTI->PR,1U<<T_PEN);
//	NVIC_SetPriority(EXTI9_5_IRQn, 20);
//	NVIC_EnableIRQ(EXTI9_5_IRQn);
	
	flash_init_mcu();
}

void xpt2046_init(void){
	flash_init();
	xpt2046_en();		// Select
	xpt2046_send(1<<XPT_S);
	xpt2046_send(0);
	xpt2046_send(0);
	delay_us(1e3);
	xpt2046_dis();	// Unselect
	flash_read(CALIB_ADDRESS,(unsigned char*) calib, CALIB_LEN);
	for(uint8_t i=0; i<CALIB_LEN/4; i++){
		if (calib[i]==-1)
			calib[i] = 1;
	}
//	calib[0] = 0x5A;
//	calib[1] = 0xFFFFFF4C;
//	calib[2] = 0x7D;
//	calib[3] = 0xFFFFFF55;	
}

char xpt2046_is_pressed(){
	return ~(GPIOC->IDR)&1U<<T_PEN;
}

short xpt2046_get(char address){
	xpt2046_send(address);
	short data = xpt2046_send(0);
	data <<= 8;
	data |= xpt2046_send(0);
	data >>= 4;
	return data;
}

int xpt2046_xy(){
	unsigned int x = 0, y = 0;
	xpt2046_en();
	for (char i=0; i<16; i++){
		if (!isTouched)
			return 0;
		y += xpt2046_get(1<<XPT_S|5<<XPT_A|0<<XPT_MODE);	// Y
		//delay_us(100);
		x += xpt2046_get(1<<XPT_S|1<<XPT_A|0<<XPT_MODE);	// X
		//delay_us(100);
	}
	xpt2046_dis();
	int xy;
//	x /= 16;
//	y /= 16;
	if(1){
		// int to Q16.16     x = a * x^ + b
//		x <<= 4;
//		y <<= 4;
		x = FDIV(x,calib[0],4);
		x = FADD(x,calib[1]);
		y = FDIV(y,calib[2],4);
		y = FADD(y,calib[3]);
		// Q16.16 to int
		xy = ((x<<12)&0xFFFF0000)|(y>>4);
	}
	else{
		xy = (x<<16)|y;
	}
	return xy;
}

char xpt2046_send(char data){
	while((SPI2->SR & SPI_SR_TXE)==0);
	SPI2->DR = data;
	while((SPI2->SR & SPI_SR_RXNE)==0);
	char in_data = SPI2->DR;
	return in_data;
}

//void EXTI9_5_IRQHandler(void){	// Touch event
//	if (EXTI->PR & 1<<T_PEN){
//		EXTI->PR = 1<<T_PEN;
////		if(!flags.touch_calibrate)
////			touch_handler();
//	}
//}

//void calib_write(void){	// Read-modify-write
//	flash_read_sector(0, 0, flash_buffer, W25Q_SECTOR_SIZE);
//	int *dst = (int*)flash_buffer+CALIB_ADDRESS;
//	int *src = (int*)calib;
//	int cnt = CALIB_LEN/4;
//	while(cnt){
//		*dst++ = *src++;
//		cnt--;
//	}
//	flash_erase_sector(0);
//	flash_write_sector(0, 0, flash_buffer, W25Q_SECTOR_SIZE);
//}
