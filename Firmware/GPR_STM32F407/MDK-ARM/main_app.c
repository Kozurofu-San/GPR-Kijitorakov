#include "stm32f407xx.h"
#include <stdio.h>
#include "usbd_cdc_if.h"
extern uint8_t UserRxBufferFS[APP_RX_DATA_SIZE], UserTxBufferFS[APP_TX_DATA_SIZE];
short *dataRe = (short*)&UserTxBufferFS[0];
short *dataIm = (short*)&UserTxBufferFS[APP_TX_DATA_SIZE/2];

#include "FreeRTOS.h"
#include "task.h"
#include "semphr.h"
#include "queue.h"
#include "timers.h"

#include "functions.h"
#include "ADF4351.h"
#include "AD9910.h"
#include "AD8302.h"
#include "VCO.h"

#include "gui.h"
#include "mathematics.h"
#include "windows.h"

xSemaphoreHandle xPrintMutex;
xQueueHandle xFrequencyQueue, xGuiBtnQueue;
xTimerHandle xPressTimer[2];
xSemaphoreHandle xRadarSem, xGuiSem, xPlaySem;

char longPressed[] = {0,0};
short frequency = 40;
short power = 0;
struct sweep_parameters_struct{
	unsigned short fmin, fmax, fstep, texp, power, phase;
};
struct sweep_parameters_struct sweep_parameters = {600,3000,10,60,3,0};

struct dsp_parameters_struct{
	unsigned short n_fft, window;
};
struct dsp_parameters_struct dsp_parameters = {512,WINDOW_RECT};

#define N_MAX FFT_N_MAX
int if_min;
short re[N_MAX], im[N_MAX];
float sMag[N_MAX], sPha[N_MAX];
float tRe[FFT_N_MAX], tIm[FFT_N_MAX];
float win[N_MAX], lpda_pha[N_MAX];

#define EVENT_RECIEVED (unsigned char)1
#define EVENT_CONNECTED (unsigned char)22
#define EVENT_DISCONNECTED (unsigned char)-5
uint8_t HANDSHAKE[] = "OK";

#define CMD_FREQUENCY (unsigned char)3
#define CMD_SWEEP_PARAMETERS (unsigned char)6
#define CMD_SWEEP (unsigned char)9
#define CMD_OSC_SELECT (unsigned char)12
#define CMD_MIX_SELECT (unsigned char)15

#define OSC_SELECT_ADF4351 (unsigned char)120
#define OSC_SELECT_AD9910 (unsigned char)121
#define OSC_SELECT_HMC830 (unsigned char)122
#define MIX_SELECT_AD8302 (unsigned char)150
#define MIX_SELECT_HMC213 (unsigned char)151
	
uint8_t osc_select = OSC_SELECT_ADF4351;

char usb_status = EVENT_DISCONNECTED;
struct flag_struct{
	uint8_t usb_status:1;
	uint8_t isRunning:1;
	uint8_t isSettingsChanged:1;
} flag = {0,0,1};

void print_log(char msg[]);

void taskMsg(void* arg){
	int cnt = 0;
	char textBuffer[100];
	while(1){
		led_turn(LED3,cnt%2);
		led_turn(LED2,cnt%2+1);
		sprintf(textBuffer,"LED %d, cnt %d\n",cnt%2,cnt);
		print_log(textBuffer);
		cnt++;
		vTaskDelay(1000/portTICK_RATE_MS);
	}
}

void print_log(char msg[]){
	xSemaphoreTake(xPrintMutex,portMAX_DELAY);
	while(*msg)
		putchar(*msg++);
	xSemaphoreGive(xPrintMutex);
}

void handshake(){
	if(usb_status==EVENT_CONNECTED)
		CDC_Transmit_FS(HANDSHAKE,sizeof(HANDSHAKE));
	else if(usb_status==EVENT_DISCONNECTED){
		xSemaphoreGive(xGuiSem);
	}
}

void taskRadar(void* arg){
	char queue;
	int n;
	while(1){
		xSemaphoreTake(xRadarSem,portMAX_DELAY);
		if(UserRxBufferFS[0]==CMD_FREQUENCY){
			handshake();
			xSemaphoreTake(xRadarSem,portMAX_DELAY);
			uint16_t* src = (uint16_t*)&UserRxBufferFS;
			uint16_t* dst = (uint16_t*)&frequency;
			*dst = *src++;
			dst = (uint16_t*)&power;
			*dst = *src;
			handshake();
			queue = 3;
			xQueueSend(xFrequencyQueue,&queue,portMAX_DELAY);
			queue = 33;
			xQueueSend(xFrequencyQueue,&queue,portMAX_DELAY);
			
		}
		else if(UserRxBufferFS[0]==CMD_SWEEP_PARAMETERS){
			handshake();
			xSemaphoreTake(xRadarSem,portMAX_DELAY);
			uint8_t* src = (uint8_t*)&UserRxBufferFS;
			uint8_t* dst = (uint8_t*)&sweep_parameters;
			for(int i=0; i<sizeof(sweep_parameters); i++)
				*dst++ = *src++;
			handshake();
		}
		else if(UserRxBufferFS[0]==CMD_SWEEP){
			n = (sweep_parameters.fmax-sweep_parameters.fmin)
				/sweep_parameters.fstep;
			short *ptr = (short*)&UserTxBufferFS[0];
			short cntLock = 0;
			adf4351_set_power(power);
			for(int f = sweep_parameters.fmin;
				f < sweep_parameters.fmax;
				f += sweep_parameters.fstep){
					adf4351_set_frequency(f);
//					vco_frequency(f);
					delay_us(sweep_parameters.texp);
					cntLock += adf4351_read_lock();
					ad8302_read(ptr,0);
					ptr += 2;
				}
			adf4351_set_power(-1);
			*ptr = cntLock;
			if(usb_status==EVENT_CONNECTED)
				CDC_Transmit_FS(UserTxBufferFS,(n*2+1)*sizeof(short));
			else
				xSemaphoreGive(xGuiSem);
		}
	}
}

void taskTestFrequency(void* arg){
	char sign;
	char step = 1;
	short data[2];
	while(1){
		xQueueReceive(xFrequencyQueue,&sign,portMAX_DELAY);
		if(sign==1 | sign==2 | sign==3){
			if(sign==2)
				frequency += step;
			if(sign==1)
				frequency -= step;
			frequency = (frequency<35)?35:frequency;
			frequency = (frequency>4400)?4400:frequency;
			adf4351_set_frequency(frequency);
//			frequency = (frequency<510)?510:frequency;
//			frequency = (frequency>1150)?1150:frequency;
//			vco_frequency(frequency);
		}
		if(sign==11 | sign==22 | sign==33){
			if(sign==22)
				power ++;
			if(sign==11)
				power --;
			power = (power<-1)?-1:power;
			power = (power>3)?3:power;
			adf4351_set_power(power);
		}
		ad8302_read(data,0);
		*data-=4;
	}
}

void taskGui(void *arg){
	int len = 0;
	while(1){
		xSemaphoreTake(xPlaySem,portMAX_DELAY);
		while(flag.isRunning){
			
			if(flag.isSettingsChanged){
				flag.isSettingsChanged = 0;
				
				UserRxBufferFS[0]=CMD_SWEEP_PARAMETERS;
				xSemaphoreGive(xRadarSem);
				xSemaphoreTake(xGuiSem,portMAX_DELAY);
				struct sweep_parameters_struct* dst = (struct sweep_parameters_struct*)&UserRxBufferFS;
//				sweep_parameters.fmin = 600;
//				sweep_parameters.fmax = 3000;
//				sweep_parameters.fstep = 10;
//				sweep_parameters.texp = 60;
//				sweep_parameters.power = 3;
//				sweep_parameters.phase = 0;
				struct sweep_parameters_struct *src = &sweep_parameters;
				*dst = *src;
				len = (sweep_parameters.fmax-sweep_parameters.fmin)/sweep_parameters.fstep;
				for(int i=0; i<N_MAX; i++){
					sMag[i] = 0;
					sPha[i] = 0;
					re[i] = 0;
					im[i] = 0;
				}
				window_compute(win,len,dsp_parameters.window);
				float lpda_min = 0.15f, lpda_max = 0.01f;
				for(int i=0; i<len; i++){
					lpda_pha[i] = 2.f*M_PI* (i*(lpda_max-lpda_min)/(float)(len-1)+lpda_min)
					* (i*(float)(sweep_parameters.fmax-sweep_parameters.fmin)/(float)(len-1)+sweep_parameters.fmin) * (1e6f/3e8f);
				}
				if_min = sweep_parameters.fmin/sweep_parameters.fstep;
				fft_init(dsp_parameters.n_fft);
				if(cur_scr==SCREEN_MAIN){
					set_x_limits(sweep_parameters.fmin, sweep_parameters.fmax,sweep_parameters.fstep);
					set_y_limits(-2000, 2000);
					set_data_re(re);
					set_data_im(im);
				}
				if(cur_scr==SCREEN_SPECTER){
					set_x_limits(sweep_parameters.fmin*0, sweep_parameters.fmax,sweep_parameters.fstep);
					set_y_limits(-2000, 2000);
					set_data_re(re+0*if_min);
					set_data_im(im+0*if_min);
				}
				else if(cur_scr==SCREEN_FFT){
					set_x_limits(0, dsp_parameters.n_fft/4,1);
					set_y_limits(-2000, 2000);
					set_data_re(re);
					set_data_im(im);
				}
				xSemaphoreGive(xRadarSem);
				xSemaphoreTake(xGuiSem,portMAX_DELAY);
			}
			
			UserRxBufferFS[0]=CMD_SWEEP;
			xSemaphoreGive(xRadarSem);
			xSemaphoreTake(xGuiSem,portMAX_DELAY);
			
			short *ptr = (short*)&UserTxBufferFS[0];
			for(int i=0; i<len; i++){
				re[i] = ptr[2*i+0];	// Mag
				im[i] = ptr[2*i+1];	// Pha
			}
//			even_odd_to_half(ptr,2*len);
			
			if(cur_scr==SCREEN_MAIN){
				update_gui();
				continue;
			}
			
			for(int i=0; i<len; i++){
				sPha[i] = (float)im[i];
				sPha[i] *= (M_PI/2.f/2048.f);;
				sPha[i] = sinf(sPha[i]);
			}
			hilbert(sPha,sMag,len,tRe,tIm);
			for(int i=0; i<len; i++){
				sPha[i] = atan2f(sMag[i],sPha[i]);
				sPha[i] -= 4*lpda_pha[i];
				sPha[i] = cosf(sPha[i]);
				tIm[0] = sinf(sMag[i]);
				
				sMag[i] = (float)re[i];
				sMag[i] *= (30.f/2048.f/20.f);
				sMag[i] = powf(10.f,sMag[i]);
				sMag[i] = powf(sMag[i],0.125f);
				sMag[i] *= win[i];
				
				tRe[0] = sMag[i];
				sMag[i] = sPha[i]*tRe[0];
				sPha[i] = tIm[0]*tRe[0];
			}
			if((len+if_min)<=N_MAX){
				for(int i=len-1; i>=0; i--){
					sPha[i+if_min] = sPha[i];
					sMag[i+if_min] = sMag[i];
				}
				for(int i=0; i<if_min; i++){
					sPha[i] = 0;
					sMag[i] = 0;
				}
			}

			if(cur_scr==SCREEN_SPECTER){
				for(int i=0; i<len+if_min; i++){
					re[i] = (short)(sMag[i]*1000);
					im[i] = (short)(sPha[i]*1000);
				}
				update_gui();
				continue;
			}
			
			fft_transform(sMag,sPha,len+if_min,tRe,tIm);
			
			for(int i=0; i<dsp_parameters.n_fft/4; i++){
				re[i] = (short)(tRe[i]*30);
//				dataIm[i] = (short)(tIm[i]*30);
				im[i] = (short)(absf(tRe[i],tIm[i])*30);
			}
			update_gui();
		}
//		xQueueReceive(xGuiBtnQueue,&cmd,portMAX_DELAY);
//		if(cmd==CMD_FREQUENCY){
//			UserRxBufferFS[0]=CMD_FREQUENCY;
//			xSemaphoreGive(xRadarSem);
//			xSemaphoreTake(xGuiSem,portMAX_DELAY);
//			uint16_t* dst = (uint16_t*)&UserRxBufferFS;
//			*dst++ = 100;
//			*dst++ = 3;
//			xSemaphoreGive(xRadarSem);
//			xSemaphoreTake(xGuiSem,portMAX_DELAY);
//		}
	}
}

void taskLongPress(xTimerHandle timer){
	portBASE_TYPE* id = pvTimerGetTimerID(timer);
	if((int)id==11)
		longPressed[0]=1;
	if((int)id==22)
		longPressed[1]=1;
}

void main_app(){
	
	NVIC_SetPriority(OTG_FS_IRQn, configLIBRARY_MAX_SYSCALL_INTERRUPT_PRIORITY+5);
	led_init();
	button_init();
	delay_init();
	if(osc_select == OSC_SELECT_ADF4351){
	adf4351_init_mcu();
	adf4351_init();
	adf4351_set_frequency(frequency);
	adf4351_set_power(power);
	}
	else if(osc_select == OSC_SELECT_AD9910){
	ad9910_init_mcu();
	ad9910_init();
	ad9910_set_frequency(frequency);
	}
	ad8302_init_mcu();
//	vco_init();
	printf("Init OK\r\n");
	
	xPrintMutex = xSemaphoreCreateMutex();
	xRadarSem = xSemaphoreCreateBinary();
	xGuiSem = xSemaphoreCreateBinary();
	xPlaySem = xSemaphoreCreateBinary();
	xFrequencyQueue = xQueueCreate(10,sizeof(char));
	xGuiBtnQueue = xQueueCreate(10,sizeof(char));
	xPressTimer[0] = xTimerCreate("Press timer",500/portTICK_RATE_MS,
		pdFALSE,(void*)11,taskLongPress);
	xPressTimer[1] = xTimerCreate("Press timer",500/portTICK_RATE_MS,
		pdFALSE,(void*)22,taskLongPress);
	xTaskCreate(taskMsg,"task msg",configMINIMAL_STACK_SIZE,NULL,3,NULL);
	xTaskCreate(taskTestFrequency,"task test",configMINIMAL_STACK_SIZE,NULL,2,NULL);
	xTaskCreate(taskGui,"task gui",configMINIMAL_STACK_SIZE,NULL,2,NULL);
	xTaskCreate(taskRadar,"task radar",configMINIMAL_STACK_SIZE+100,NULL,2,NULL);
	gui_init();
	vTaskStartScheduler();
	while(1);
}

void button0_press_callback(){
	xTimerStartFromISR(xPressTimer[0],NULL);
}

void button1_press_callback(){
	xTimerStartFromISR(xPressTimer[1],NULL);
}

void button0_release_callback(){
	char sign = 1;
	if(longPressed[0]){
		sign = 11;
		longPressed[0] = 0;
	}
	else
		xTimerStopFromISR(xPressTimer[0],NULL);
	xQueueSendFromISR(xFrequencyQueue,(void*)&sign,NULL);
}

void button1_release_callback(){
	char sign = 2;
	if(longPressed[1]){
		sign = 22;
		longPressed[1] = 0;
	}
	else
		xTimerStopFromISR(xPressTimer[1],NULL);
	xQueueSendFromISR(xFrequencyQueue,(void*)&sign,NULL);
}

void gui_button_play_callback(){	// gui.c
	if(!flag.isRunning){
		flag.isRunning = 1;
		xSemaphoreGiveFromISR(xPlaySem,NULL);
	}
	else{
		flag.isRunning = 0;	
	}
}

void gui_btn_switch_callback(){	// gui.c
	flag.isSettingsChanged = 1;
}

void gui_btn_menu_callback(){	// gui.c
	flag.isRunning = 0;
	flag.isSettingsChanged = 1;
}


void usb_callback(){	// usbd_cdc_if.c -> CDC_Receive_FS
	xSemaphoreGiveFromISR(xRadarSem,NULL);
//	usb_status = EVENT_RECIEVED;
}

void usb_connected(){	// usbd_cdc_if.c -> CDC_Init_FS
	usb_status = EVENT_CONNECTED;
	set_buttons_visible(0);
	flag.isRunning = 0;	
}

void usb_disconnected(){	// usbd_conf.c -> HAL_PCD_SuspendCallback
	if(usb_status == EVENT_CONNECTED)
		NVIC_SystemReset();
	usb_status = EVENT_DISCONNECTED;
	set_buttons_visible(1);
}