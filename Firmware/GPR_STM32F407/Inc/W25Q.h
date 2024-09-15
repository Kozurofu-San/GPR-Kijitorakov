// Flash W25Q SPI1
#define FLASH_CS 0		// PB0
#define FLASH_CLK 3		// PB3
#define FLASH_DO 4		// PB4
#define FLASH_DIO 5		// PB5

void flash_init_mcu(void);
void flash_init(void);
unsigned char flash_send(unsigned char data);
void flash_reset(void);
unsigned int flash_id(void);
void flash_read(unsigned int address,
	unsigned char *data, unsigned int size);
void flash_read_page(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len);
void flash_read_sector(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len);
void flash_read_block(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len);
void flash_write(unsigned int address,
	unsigned char *data, unsigned int len);
void flash_write_page(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len);
void flash_write_sector(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len);
void flash_write_block(unsigned int address, unsigned int offset,
	unsigned char *data, unsigned int len);
void flash_wait(void);
void flash_erase_chip(void);
void flash_erase_sector(unsigned int address);
void flash_erase_block(unsigned int address);

#define W25Q_PAGE_SIZE 256
#define W25Q_SECTOR_SIZE 4096
#define W25Q_BLOCK_SIZE W25Q_SECTOR_SIZE*16
//#define W25Q_BLOCK_COUNT 1024			// W25Q512
//#define W25Q_BLOCK_COUNT 512			// W25Q256
//#define W25Q_BLOCK_COUNT 256			// W25Q128
//#define W25Q_BLOCK_COUNT 128			// W25Q64
//#define W25Q_BLOCK_COUNT 64			// W25Q32
#define W25Q_BLOCK_COUNT 32			// W25Q16
//#define W25Q_BLOCK_COUNT 16			// W25Q80
//#define W25Q_BLOCK_COUNT 8			// W25Q40
//#define W25Q_BLOCK_COUNT 4			// W25Q20
//#define W25Q_BLOCK_COUNT 2			// W25Q10
#if W25Q_BLOCK_COUNT >= 512
#define W25Q_HIGH_CAP 1
#else 
#define W25Q_HIGH_CAP 0
#endif
#define W25Q_SECTOR_COUNT W25Q_BLOCK_COUNT*16
#define W25Q_PAGE_COUNT (W25Q_SECTOR_COUNT*W25Q_SECTOR_SIZE)/W25Q_PAGE_SIZE
#define W25Q_NUM_KB (W25Q_SECTOR_COUNT*W25Q_SECTOR_SIZE)/1024
	
// Status register
#define W25Q_STATUS_BUSY 0	// WRITE IN PROGRESS
#define W25Q_STATUS_WEL 1		// WRITE ENABLE LATCH
#define W25Q_STATUS_BP0 2		// BLOCK PROTECT BITS
#define W25Q_STATUS_BP1 3
#define W25Q_STATUS_BP2 4
#define W25Q_STATUS_TB 5		// TOP/BOTTOM PROTECT
#define W25Q_STATUS_SEC 6		// SECTOR PROTECT
#define W25Q_STATUS_SRP 7		// STATUS REGISTER PROTECT
	
	
// Standard SPI Instructions
#define W25Q_WRITE_ENABLE 0x06
#define W25Q_VOLATILE_SR_WRITE_ENABLE 0x50
#define W25Q_WRITE_DISABLE 0x04

#define W25Q_RELEASE_POWER_DOWN_DEVICE_ID 0xAB
#define W25Q_MANUFACTURER_DEVICE_ID 0x90
#define W25Q_JEDEC_ID 0x9F
#define W25Q_READ_UNIQUE_ID 0x4B

#define W25Q_READ_DATA 0x03
#define W25Q_FAST_READ 0x0B

#define W25Q_PAGE_PROGRAM 0x02

#define W25Q_SECTOR_ERASE_4KB 0x20
#define W25Q_BLOCK_ERASE_32KB 0x52
#define W25Q_BLOCK_ERASE_64KB 0xD8
#define W25Q_CHIP_ERASE 0xC7
//#define W25Q_CHIP_ERASE 0x60

#define W25Q_READ_STATUS_REGISTER1 0x05
#define W25Q_WRITE_STATUS_REGISTER1 0x01
#define W25Q_READ_STATUS_REGISTER2 0x35
#define W25Q_WRITE_STATUS_REGISTER2 0x31
#define W25Q_READ_STATUS_REGISTER3 0x15
#define W25Q_WRITE_STATUS_REGISTER3 0x11

#define W25Q_READ_SFDP_REGISTER 0x5A
#define W25Q_ERASE_SECURITY_REGISTER 0x44
#define W25Q_PROGRAM_SECURITY_REGISTER 0x42
#define W25Q_READ_SECURITY_REGISTER 0x48

#define W25Q_GLOBAL_BLOCK_LOCK 0x7E
#define W25Q_GLOBAL_BLOCK_UNLOCK 0x98
#define W25Q_READ_BLOCK_LOCK 0x3D
#define W25Q_INDIVIDUAL_BLOCK_LOCK 0x36
#define W25Q_INDIVIDUAL_BLOCK_UNLOCK 0x39

#define W25Q_ERASE_PROGRAM_SUSPEND 0x75
#define W25Q_ERASE_PROGRAM_RESUME 0x7A
#define W25Q_POWER_DOWN 0xB9

#define W25Q_ENABLE_RESET 0x66
#define W25Q_RESET_DEVICE 0x99

// Dual/Quad SPI Instructions
#define W25Q_FAST_READ_DUAL_OUTPUT 0x3B
#define W25Q_FAST_READ_DUAL_IO 0xBB
#define W25Q_MANUFACTURER_DEVICE_ID_DUAL 0x92

#define W25Q_QUAD_PAGE_PROGRAM 0x32
#define W25Q_FAST_READ_QUAD_OUTPUT 0x6B
#define W25Q_MANUFACTURER_DEVICE_ID_QUAD 0x94
#define W25Q_FAST_READ_QUAD_IO 0xEB
#define W25Q_SET_BURST_WITH_WRAP 0x77
