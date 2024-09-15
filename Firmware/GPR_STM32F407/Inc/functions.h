#include "stm32f407xx.h"

void delay_init(void);
void delay_us(unsigned int t);
void timer_base_init(void);
void timer_init(int ms);
void timer_callback();
void led_init(void);
void led_turn(char n,int arg);
long long bin2bcd(int bin);
void button0_press_callback();
void button0_release_callback();
void button1_press_callback();
void button1_release_callback();


// LED on (F4VE)
#define LED2 6	// PA6
#define LED3 7	// PA7

#define BUT0 4	// PE4
#define BUT1 3	// PE3

typedef struct
{
	GPIO_TypeDef* GPIOx;
	uint8_t PIN;
	uint8_t MODE;
	uint8_t SPEED;
	uint8_t PULL;
	uint8_t AF;
	uint8_t STATE;
} tGPIO_Line;
void configureIO(const tGPIO_Line *Line, uint8_t len);
void deconfigureIO(const tGPIO_Line *Line, uint8_t len);
#define confIO(arg) (configureIO(arg, sizeof(arg)/sizeof(tGPIO_Line)))
#define deconfIO(arg) (configureIO(arg, sizeof(arg)/sizeof(tGPIO_Line)))

void button_init(void);

void sleep_init(void);

struct rtc_struct{		// BCD format
		 unsigned char year : 8; 	
		 unsigned char month : 5;
		 unsigned char week : 3; 
		 unsigned char date : 6;
		 unsigned char hour : 6;
		 unsigned char min : 7;
		 unsigned char sec : 7;
};
void rtc_update(struct rtc_struct *value);
void rtc_init(struct rtc_struct *value);

void even_odd_to_half(volatile short p[], uint32_t len);

/* The basic operations perfomed on two numbers a and b of fixed
 point q format returning the answer in q format */
#define FADD(a,b) ((a)+(b))
#define FSUB(a,b) ((a)-(b))
#define FMUL(a,b,q) (((a)*(b))>>(q))
#define FDIV(a,b,q) (((a)<<(q))/(b))
/* The basic operations where a is of fixed point q format and b is
 an integer */
#define FADDI(a,b,q) ((a)+((b)<<(q)))
#define FSUBI(a,b,q) ((a)-((b)<<(q)))
#define FMULI(a,b) ((a)*(b))
#define FDIVI(a,b) ((a)/(b))
/* convert a from q1 format to q2 format */
#define FCONV(a, q1, q2) (((q2)>(q1)) ? (a)<<((q2)-(q1)) : (a)>>((q1)-(q2)))
/* the general operation between a in q1 format and b in q2 format
 returning the result in q3 format */
#define FADDG(a,b,q1,q2,q3) (FCONV(a,q1,q3)+FCONV(b,q2,q3))
#define FSUBG(a,b,q1,q2,q3) (FCONV(a,q1,q3)-FCONV(b,q2,q3))
#define FMULG(a,b,q1,q2,q3) FCONV((a)*(b), (q1)+(q2), q3)
#define FDIVG(a,b,q1,q2,q3) (FCONV(a, q1, (q2)+(q3))/(b))
/* convert to and from floating point */
#define TOFIX(d, q) ((int)( (d)*(double)(1<<(q)) ))
#define TOFLT(a, q) ( (double)(a) / (double)(1<<(q)) )
