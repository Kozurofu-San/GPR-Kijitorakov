#include "stm32f4xx.h"                  // Device header
#include "W25Q.h"
#include "functions.h"

#define flash_en() GPIOB->BSRR = (1<<FLASH_CS)<<16
#define flash_dis() GPIOB->BSRR = (1<<FLASH_CS)<<0
#define flash_write_en() {\
	flash_en();\
	flash_send(W25Q_WRITE_ENABLE);\
	flash_dis();\
}
#define flash_write_dis() {\
	flash_en();\
	flash_send(W25Q_WRITE_DISABLE);\
	flash_dis();\
}

void flash_init_mcu(void){
	SET_BIT(RCC->AHB1ENR,RCC_AHB1ENR_GPIOBEN);
	WRITE_REG(GPIOB->BSRR,(1<<FLASH_CS|1<<FLASH_CLK|1<<FLASH_DO|1<<FLASH_DIO)<<16);
	MODIFY_REG(GPIOB->MODER,3<<FLASH_CS*2|3<<FLASH_CLK*2|3<<FLASH_DO*2|3<<FLASH_DIO*2,
		(GPIO_MODE_OUTPUT_PP&0xF)<<FLASH_CS*2|
		(GPIO_MODE_AF_PP&0xF)<<FLASH_CLK*2|
		(GPIO_MODE_AF_PP&0xF)<<FLASH_DO*2|
		(GPIO_MODE_AF_PP&0xF)<<FLASH_DIO*2);
	MODIFY_REG(GPIOB->OTYPER,1<<FLASH_CS|1<<FLASH_CLK|1<<FLASH_DO|1<<FLASH_DIO,
		(GPIO_MODE_OUTPUT_PP>>4)<<FLASH_CS|
		(GPIO_MODE_AF_PP>>4)<<FLASH_CLK|
		(GPIO_MODE_AF_PP>>4)<<FLASH_DO|
		(GPIO_MODE_AF_PP>>4)<<FLASH_DIO);
	MODIFY_REG(GPIOB->OSPEEDR,3<<FLASH_CS*2|3<<FLASH_CLK*2|3<<FLASH_DO*2|3<<FLASH_DIO*2,
		GPIO_SPEED_FREQ_VERY_HIGH<<FLASH_CS*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<FLASH_CLK*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<FLASH_DO*2|
		GPIO_SPEED_FREQ_VERY_HIGH<<FLASH_DIO*2);
	MODIFY_REG(GPIOB->PUPDR,3<<FLASH_CS*2|3<<FLASH_CLK*2|3<<FLASH_DO*2|3<<FLASH_DIO*2,
		GPIO_PULLUP<<FLASH_CS*2|
		GPIO_PULLUP<<FLASH_CLK*2|
		GPIO_PULLUP<<FLASH_DO*2|
		GPIO_PULLUP<<FLASH_DIO*2);
	MODIFY_REG(GPIOB->AFR[0],15<<FLASH_CLK*4|15<<FLASH_DO*4|15<<FLASH_DIO*4,
		GPIO_AF5_SPI1<<FLASH_CLK*4|
		GPIO_AF5_SPI1<<FLASH_DO*4|
		GPIO_AF5_SPI1<<FLASH_DIO*4);			// SPI pins
	// SPI
	// W25Qxx max CLK speed is 104 MHz
	// 8 bit, CPOL=0, CPHA=0, MBR, BR=48 MHz
	SET_BIT(RCC->APB2ENR,RCC_APB2ENR_SPI1EN);
	WRITE_REG(SPI1->CR1,0*SPI_CR1_DFF|SPI_CR1_SSM|SPI_CR1_SSI|SPI_CR1_MSTR|0<<SPI_CR1_BR_Pos);
	SET_BIT(SPI1->CR1,SPI_CR1_SPE);		// Turn on
}

void flash_init(void){
	int id = flash_id();
	delay_us(100*1e3);
	flash_en();
	flash_send(W25Q_ENABLE_RESET);
	flash_dis();
	flash_en();
	flash_send(W25Q_RESET_DEVICE);
	flash_dis();
	delay_us(100*1e3);
}

unsigned int flash_id(void){
	flash_en();
	flash_send(W25Q_JEDEC_ID);
	int id = flash_send(0);
	id <<= 8;
	id |= flash_send(0);
	id <<= 8;
	id |= flash_send(0);
	flash_dis();
	return id;
}

void flash_read(unsigned int address,
	unsigned char *data, unsigned int len){
	flash_en();
	flash_send(W25Q_READ_DATA);
	#if W25Q_HIGH_CAP
	flash_send(address>>24);		// 3 byte
	#endif
	flash_send((address>>16)&0xFF);	// 2 byte
	flash_send((address>>8)&0xFF);	// 1 byte 
	flash_send(address&0xFF);		// 0 byte
	*data = flash_send(0);	// Read bytes
	while(len--){
		*data++ = flash_send(0);	// Read bytes
	}
	flash_dis();
}

void flash_read_page(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len){
	if(len > W25Q_PAGE_SIZE)
		len = W25Q_PAGE_SIZE;
	if((len+offset) > W25Q_PAGE_SIZE)
		len = W25Q_PAGE_SIZE-offset;
	address = address*W25Q_PAGE_SIZE+offset;
	flash_en();
	flash_send(W25Q_FAST_READ);
	#if W25Q_HIGH_CAP
	flash_send(address>>24);		// 3 byte
	#endif
	flash_send((address>>16)&0xFF);	// 2 byte
	flash_send((address>>8)&0xFF);	// 1 byte 
	flash_send(address&0xFF);		// 0 byte
	flash_send(0);
	while(len--){
		*data++ = flash_send(0);	// Read bytes
	}
	flash_dis();
}

void flash_read_sector(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len){
	if(len > W25Q_SECTOR_SIZE)
		len = W25Q_SECTOR_SIZE;
	if((len+offset) > W25Q_SECTOR_SIZE)
		len = W25Q_SECTOR_SIZE-offset;
	address = (address*W25Q_SECTOR_SIZE+offset)/W25Q_PAGE_SIZE;
	offset %= W25Q_PAGE_SIZE;
	do{
		flash_read_page(address, offset, data, len);
		address++;
		len -= W25Q_PAGE_SIZE-offset;
		data += W25Q_PAGE_SIZE-offset;
		offset = 0;
	} while(len>0);
}

void flash_read_block(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len){
	if(len > W25Q_BLOCK_SIZE)
		len = W25Q_BLOCK_SIZE;
	if((len+offset) > W25Q_SECTOR_SIZE)
		len = W25Q_BLOCK_SIZE-offset;
	address = (address*W25Q_BLOCK_SIZE+offset)/W25Q_PAGE_SIZE;
	offset %= W25Q_PAGE_SIZE;
	do{
		flash_read_page(address, offset, data, len);
		address++;
		len -= W25Q_PAGE_SIZE-offset;
		data += W25Q_PAGE_SIZE-offset;
		offset = 0;
	} while(len>0);
}
	
void flash_write(unsigned int address,
	unsigned char *data, unsigned int len){
	flash_write_en();
	flash_en();
	flash_send(W25Q_PAGE_PROGRAM);
	#if W25Q_HIGH_CAP
	flash_send(address>>24);		// 3 byte
	#endif
	flash_send((address>>16)&0xFF);	// 2 byte
	flash_send((address>>8)&0xFF);	// 1 byte 
	flash_send(address&0xFF);		// 0 byte
	while(len--){
		flash_send(*data++);		// Write bytes
	}
	flash_dis();
	flash_wait();
	flash_write_dis();
}

void flash_write_page(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len){
	if(len > W25Q_PAGE_SIZE)
		len = W25Q_PAGE_SIZE;
	if((len+offset) > W25Q_PAGE_SIZE)
		len = W25Q_PAGE_SIZE-offset;
	address = address*W25Q_PAGE_SIZE+offset;
	flash_write_en();
	flash_en();
	flash_send(W25Q_PAGE_PROGRAM);
	#if W25Q_HIGH_CAP
	flash_send(address>>24);		// 3 byte
	#endif
	flash_send((address>>16)&0xFF);	// 2 byte
	flash_send((address>>8)&0xFF);	// 1 byte 
	flash_send(address&0xFF);		// 0 byte
	while(len--){
		flash_send(*data++);		// Write bytes
	}
	flash_dis();
	flash_wait();	
	flash_write_dis();
}

void flash_write_sector(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len){
	if(len > W25Q_SECTOR_SIZE)
		len = W25Q_SECTOR_SIZE;
	if((len+offset) > W25Q_SECTOR_SIZE)
		len = W25Q_SECTOR_SIZE-offset;
	address = (address*W25Q_SECTOR_SIZE+offset)/W25Q_PAGE_SIZE;
	flash_write_en();
	do{
		flash_write_page(address, offset, data, len);
		address++;
		len -= W25Q_PAGE_SIZE-offset;
		data += W25Q_PAGE_SIZE-offset;
		offset = 0;
	} while(len>0);
	flash_wait();	
	flash_write_dis();
}

void flash_write_block(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len){
	if(len > W25Q_BLOCK_SIZE)
		len = W25Q_BLOCK_SIZE;
	if((len+offset) > W25Q_BLOCK_SIZE)
		len = W25Q_BLOCK_SIZE-offset;
	address = (address*W25Q_BLOCK_SIZE+offset)/W25Q_PAGE_SIZE;
	flash_write_en();
	do{
		flash_write_page(address, offset, data, len);
		address++;
		len -= W25Q_PAGE_SIZE-offset;
		data += W25Q_PAGE_SIZE-offset;
		offset = 0;
	} while(len>0);
	flash_wait();	
	flash_write_dis();
}
	
unsigned char flash_send(unsigned char data){
	while((SPI1->SR & SPI_SR_TXE)==0){}
	SPI1->DR = data;
	while((SPI1->SR & SPI_SR_RXNE)==0){}
	char in_data = SPI1->DR;
	return in_data;
}

void flash_chip_erase(void){
	flash_write_en();
	flash_en();
	flash_send(W25Q_CHIP_ERASE);
	flash_dis();
	flash_wait();
	flash_write_dis();
}
void flash_erase_sector(unsigned int address){
	address *= W25Q_SECTOR_SIZE;
	flash_write_en();
	flash_en();
	flash_send(W25Q_SECTOR_ERASE_4KB);
	#if W25Q_HIGH_CAP
	flash_send(address>>24);		// 3 byte
	#endif
	flash_send((address>>16)&0xFF);	// 2 byte
	flash_send((address>>8)&0xFF);	// 1 byte 
	flash_send(address&0xFF);		// 0 byte
	flash_dis();
	flash_wait();
	flash_write_dis();
}

void flash_erase_block(unsigned int address){
	address *= W25Q_BLOCK_SIZE;
	flash_write_en();
	flash_en();
	flash_send(W25Q_BLOCK_ERASE_64KB);
	#if W25Q_HIGH_CAP
	flash_send(address>>24);		// 3 byte
	#endif
	flash_send((address>>16)&0xFF);	// 2 byte
	flash_send((address>>8)&0xFF);	// 1 byte 
	flash_send(address&0xFF);		// 0 byte
	flash_dis();
	flash_wait();
	flash_write_dis();
}

void flash_wait(void){
	flash_en();
	char sts_reg;
	do{
		flash_send(W25Q_READ_STATUS_REGISTER1);
		sts_reg = flash_send(0);
	} while((sts_reg & (1<<W25Q_STATUS_BUSY))>0);
	flash_dis();
}
