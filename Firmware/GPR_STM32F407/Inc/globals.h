// Global

extern char UserRxBufferFS[];
#define buffer UserRxBufferFS
extern char UserTxBufferFS[];
#define radar_data UserTxBufferFS


extern volatile char touch_calibrate_flag;
extern volatile char timer_base_flag;
extern volatile char time_update_flag;
extern volatile char date_update_flag;
extern char touch_flag;
struct flags_struct{
	char touch_calibrate:1;
	char timer_base:1;
	char time_update:1;
	char date_update:1;
	char touch:1;
};
extern struct flags_struct flags;

struct giu_flags_struct{
	char buttonOK: 1;
	char buttonBack: 1;
	char buttonPaint: 1;
	char buttonSettings: 1;
	char buttonScope: 1;
};
extern struct giu_flags_struct gui_flags;

extern struct rtc_struct rtc;

extern int calib[4];
extern unsigned char flash_buffer[4096];
#define CALIB_ADDRESS 0
#define CALIB_LEN sizeof(calib)
