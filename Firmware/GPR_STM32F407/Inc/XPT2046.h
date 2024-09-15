// Pins
#define T_CS 12		// PB12 - chip select
#define T_PEN 5		// PC5 - touch pen
#define T_SCK 13	// PB13
#define T_MISO 14	// PC14
#define T_MOSI 15	// PB15

#define isTouched ((GPIOC->IDR & 1U<<T_PEN)==0)

// Prototypes
void xpt2046_init_mcu(void);
void xpt2046_init(void);
char xpt2046_is_pressed();
int xpt2046_xy();

#define XPT_S 7
#define XPT_A 4
#define XPT_MODE 3
#define XPT_SER_DFR 2
#define XPT_PD 0
