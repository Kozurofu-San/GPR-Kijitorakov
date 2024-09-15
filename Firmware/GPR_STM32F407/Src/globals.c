// Global
#include "functions.h"
#include "globals.h"

volatile char timer_base_flag = 0;
volatile char touch_calibrate_flag = 0;
volatile char time_update_flag = 0;
volatile char date_update_flag = 0;
char touch_flag = 0;
struct rtc_struct rtc;
struct flags_struct flags;
struct giu_flags_struct gui_flags;
int calib[4];	// ax, bx, ay, by
unsigned char flash_buffer[4096] __attribute__((aligned (8)));

