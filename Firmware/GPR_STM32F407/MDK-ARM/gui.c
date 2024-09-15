#include "functions.h"
#include "ILI9341.h"
#include "XPT2046.h"
#include "lvgl.h"

#include "gui.h"
#include "windows.h"

#include "FreeRTOS.h"
#include "semphr.h"
#include "task.h"

#include <stdlib.h>
#include <stdio.h>

#define DISP_HOR_RES 320
#define DISP_VER_RES 240
#define UPDATE_PERIOD 20

#define LVGL_LOOP 1
#define LVGL_MATRIX 2
#define LVGL_DMA 3
#define LVGL_UPDATE LVGL_MATRIX

struct sweep_parameters_struct{
	short fmin, fmax, fstep, texp, power, phase;
};
extern struct sweep_parameters_struct sweep_parameters;

struct dsp_parameters_struct{
	unsigned short n_fft, window;
};
extern struct dsp_parameters_struct dsp_parameters;

void my_disp_flush(lv_disp_drv_t * disp, const lv_area_t * area, lv_color_t * color_p);
void my_touchpad_read(struct _lv_indev_drv_t * indev, lv_indev_data_t * data);
void set_buttons_visible(char);

static lv_obj_t *scr_main;
static lv_obj_t * chart;
static lv_chart_series_t *serMag,*serPha;
		
static lv_obj_t *btn_play,*btn_settings,*btn_plus,*btn_minus,*btn_switch;
static lv_obj_t *btn_label;

static lv_obj_t *scr_menu;
static lv_obj_t *menu;
enum {
    LV_MENU_ITEM_BUILDER_VARIANT_1,
    LV_MENU_ITEM_BUILDER_VARIANT_2
};
typedef uint8_t lv_menu_builder_variant_t;

static void back_event_handler(lv_event_t * e);
lv_obj_t * root_page;
static lv_obj_t * create_text(lv_obj_t * parent, const char * icon, const char * txt,
                              lv_menu_builder_variant_t builder_variant);
static lv_obj_t * create_label(lv_obj_t * parent, const char * icon, const char * txt_left, const char * txt_right);
static void settings_event_handler(lv_event_t * e);
static void btnm_event_handler(lv_event_t * e);
static void textarea_event_handler(lv_event_t * e);
		
lv_obj_t * btnm;
lv_obj_t * ta;
lv_obj_t *cur_label;

static int x_begin = 0, x_end = 1, x_step = 1, x_n = 5;
static int y_n = 9;

char cur_scr = SCREEN_MAIN;

void update_gui(){
	lv_chart_refresh(chart);
//	lv_obj_invalidate(scr_main);
	lv_refr_now(NULL);
}

void timer_callback(){
	lv_tick_inc(UPDATE_PERIOD);
	lv_task_handler();
}

void scroll_event_cb(lv_event_t * e)
{
//	lv_obj_t * cont = lv_event_get_target(e);
//	update_gui();
}

void set_buttons_visible(char arg){
	if(!arg){
		lv_obj_add_flag(btn_play,LV_OBJ_FLAG_HIDDEN);
		lv_obj_add_flag(btn_settings,LV_OBJ_FLAG_HIDDEN);
		lv_obj_add_flag(btn_plus,LV_OBJ_FLAG_HIDDEN);
		lv_obj_add_flag(btn_minus,LV_OBJ_FLAG_HIDDEN);
		lv_obj_add_flag(btn_switch,LV_OBJ_FLAG_HIDDEN);		
	} else {
		lv_obj_clear_flag(btn_play,LV_OBJ_FLAG_HIDDEN);
		lv_obj_clear_flag(btn_settings,LV_OBJ_FLAG_HIDDEN);
		lv_obj_clear_flag(btn_plus,LV_OBJ_FLAG_HIDDEN);
		lv_obj_clear_flag(btn_minus,LV_OBJ_FLAG_HIDDEN);
		lv_obj_clear_flag(btn_switch,LV_OBJ_FLAG_HIDDEN);
	}
}

void set_x_limits(uint32_t min, uint32_t max, uint32_t step){
	x_begin = min;
	x_end = max;
	x_step = (max-min)/4;
	lv_chart_set_point_count(chart, (x_end-x_begin)/step);
	int len = (max-min)/step;
}

void set_data_re(short *data){
	lv_chart_set_ext_y_array(chart, serMag, data);
}

void set_data_im(short *data){
	lv_chart_set_ext_y_array(chart, serPha, data);
}

static uint16_t zoom_factor = 256;
static void event_handler(lv_event_t * e)
{
    lv_event_code_t code = lv_event_get_code(e);
		lv_obj_t *target = lv_event_get_current_target(e);
	
		if(target==btn_play){
			if(code == LV_EVENT_CLICKED) {
					gui_button_play_callback();
			}
			else if(code == LV_EVENT_VALUE_CHANGED) {
			}
		}
		else if(target==btn_settings){
			if(code == LV_EVENT_CLICKED) {
				gui_btn_switch_callback();
				lv_scr_load(scr_menu);
			}
		}
		else if(target==btn_plus){
			if(code == LV_EVENT_CLICKED) {
				if(zoom_factor<256*10){
					zoom_factor*=2;
					x_n*=2;
					y_n*=2;
					x_step = (x_end-x_begin)/(x_n-1);
					lv_chart_set_zoom_x(chart,zoom_factor);
					lv_chart_set_zoom_y(chart,zoom_factor);
					lv_chart_set_axis_tick(chart, LV_CHART_AXIS_PRIMARY_X, -5, 4, x_n, 2, true, 20);
					lv_chart_set_axis_tick(chart, LV_CHART_AXIS_PRIMARY_Y, -5, 4, y_n, 2, true, 50);
				}
			}
		}
		else if(target==btn_minus){
			if(code == LV_EVENT_CLICKED) {
				if(zoom_factor>256){
					zoom_factor/=2;;
					x_n/=2;
					y_n/=2;
					x_step = (x_end-x_begin)/(x_n-1);
					lv_chart_set_zoom_x(chart,zoom_factor);
					lv_chart_set_zoom_y(chart,zoom_factor);
					lv_chart_set_axis_tick(chart, LV_CHART_AXIS_PRIMARY_X, -5, 4, x_n, 2, true, 20);
					lv_chart_set_axis_tick(chart, LV_CHART_AXIS_PRIMARY_Y, -5, 4, y_n, 2, true, 50);
				}
			}
		}
		else if(target == btn_switch){
			if(code == LV_EVENT_CLICKED) {
				if(cur_scr == SCREEN_MAIN){
//					lv_label_set_text(btn_switch, LV_SYMBOL_PREV);
					cur_scr = SCREEN_SPECTER;
				}
				else if(cur_scr == SCREEN_SPECTER){
//					lv_label_set_text(btn_switch, LV_SYMBOL_NEXT);
					cur_scr = SCREEN_FFT;
				}
				else if(cur_scr == SCREEN_FFT){
//					lv_label_set_text(btn_switch, LV_SYMBOL_NEXT);
					cur_scr = SCREEN_MAIN;
				}
				gui_btn_switch_callback();
			}
		}
		
}

static void draw_x_axis(lv_event_t * e){
	lv_obj_draw_part_dsc_t * dsc = lv_event_get_draw_part_dsc(e);
    if(!lv_obj_draw_part_check_type(dsc, &lv_chart_class, LV_CHART_DRAW_PART_TICK_LABEL)) return;

    if(dsc->id == LV_CHART_AXIS_PRIMARY_X && dsc->text){
      lv_snprintf(dsc->text, dsc->text_length, "%d", dsc->value*x_step+x_begin);
		}
}

void set_y_limits(int min, int max){
	lv_chart_set_range(chart, LV_CHART_AXIS_PRIMARY_Y, min, max);
}

struct label_list_s{
	lv_obj_t *fmin;
	lv_obj_t *fmax;
	lv_obj_t *fstep;
	lv_obj_t *texp;
	lv_obj_t *power;
	lv_obj_t *phase;
	lv_obj_t *f;
	lv_obj_t *p;
	lv_obj_t *nfft;
	lv_obj_t *window;
}label_list;
void menu_get_values(){
		sweep_parameters.fmin = (uint16_t)atoi(lv_label_get_text(label_list.fmin));
		sweep_parameters.fmax = (uint16_t)atoi(lv_label_get_text(label_list.fmax));
		sweep_parameters.fstep = (uint16_t)atoi(lv_label_get_text(label_list.fstep));
		sweep_parameters.texp = (uint16_t)atoi(lv_label_get_text(label_list.texp));
		sweep_parameters.power = (uint16_t)atoi(lv_label_get_text(label_list.power));
		sweep_parameters.phase = (uint16_t)atoi(lv_label_get_text(label_list.phase));
}
void menu_set_values(){
		static char buf[10];
		sprintf(buf,"%d",sweep_parameters.fmin);
		lv_label_set_text(label_list.fmin,buf);
		lv_label_set_text(label_list.f,buf);
		sprintf(buf,"%d",sweep_parameters.fmax);
		lv_label_set_text(label_list.fmax,buf);
		sprintf(buf,"%d",sweep_parameters.fstep);
		lv_label_set_text(label_list.fstep,buf);
		sprintf(buf,"%d",sweep_parameters.texp);
		lv_label_set_text(label_list.texp,buf);
		sprintf(buf,"%d",sweep_parameters.power);
		lv_label_set_text(label_list.power,buf);
		lv_label_set_text(label_list.p,buf);
		sprintf(buf,"%d",sweep_parameters.phase);
		lv_label_set_text(label_list.phase,buf);
		sprintf(buf,"%d",dsp_parameters.n_fft);
		lv_label_set_text(label_list.nfft,buf);
		sprintf(buf,"%d",dsp_parameters.window);
		lv_label_set_text(label_list.window,buf);
}

void gui_init(){
	// Peripherals
	delay_init();
	timer_init(UPDATE_PERIOD);
	ili9341_init_mcu();
	ili9341_init(DISP_HOR_RES,DISP_VER_RES);
	xpt2046_init_mcu();
	xpt2046_init();
	
	// LV Display
	lv_init();
	
	static lv_disp_draw_buf_t draw_buf;
	static lv_color_t buf1[DISP_HOR_RES * DISP_VER_RES / 10];
	lv_disp_draw_buf_init(&draw_buf, buf1, NULL, DISP_HOR_RES * DISP_VER_RES / 10);
	
	static lv_disp_drv_t disp_drv;
	lv_disp_drv_init(&disp_drv);          /*Basic initialization*/
	disp_drv.flush_cb = my_disp_flush;    /*Set your driver function*/
	disp_drv.draw_buf = &draw_buf;        /*Assign the buffer to the display*/
	disp_drv.hor_res = DISP_HOR_RES;   /*Set the horizontal resolution of the display*/
	disp_drv.ver_res = DISP_VER_RES;   /*Set the vertical resolution of the display*/
	lv_disp_drv_register(&disp_drv);      /*Finally register the driver*/
	
	// LV Input
	static lv_indev_drv_t indev_drv;           /*Descriptor of a input device driver*/
	lv_indev_drv_init(&indev_drv);             /*Basic initialization*/
	indev_drv.type = LV_INDEV_TYPE_POINTER;    /*Touch pad is a pointer-like device*/
	indev_drv.read_cb = &my_touchpad_read;      /*Set your driver function*/
	lv_indev_drv_register(&indev_drv);         /*Finally register the driver*/
	
		// GUI
		
    // Screen*********************************************************************
    scr_main = lv_obj_create(NULL);

		// Background
		lv_obj_set_style_bg_color(scr_main,lv_color_white(),LV_PART_MAIN);
		
		// Chart
		static lv_style_t style;
		lv_style_set_radius(&style, 0);
    lv_style_set_border_width(&style, 0);
    lv_style_set_bg_opa(&style, LV_OPA_50);
    lv_style_set_bg_color(&style, lv_color_white());
		lv_style_set_line_width(&style,1);
		lv_style_set_line_dash_width(&style,5);
		lv_style_set_line_dash_gap(&style,5);
		
    chart = lv_chart_create(scr_main);
		lv_obj_add_style(chart, &style, 0);
    lv_obj_set_size(chart, 280, 230);
    lv_obj_align(chart, LV_ALIGN_TOP_RIGHT, 0, 0);
		set_y_limits(-2000, 2000);
		lv_chart_set_axis_tick(chart, LV_CHART_AXIS_PRIMARY_Y, -5, 4, y_n, 2, true, 50);
		lv_chart_set_axis_tick(chart, LV_CHART_AXIS_PRIMARY_X, -5, 4, x_n, 2, true, 20);
		lv_obj_add_event_cb(chart, draw_x_axis, LV_EVENT_DRAW_PART_BEGIN, NULL);
		lv_obj_set_style_line_width(chart,1,LV_PART_ITEMS);
		lv_obj_add_event_cb(chart, scroll_event_cb, LV_EVENT_SCROLL, NULL);

    /*Do not display points on the data*/
    lv_obj_set_style_size(chart, 0, LV_PART_INDICATOR);
		
    serMag = lv_chart_add_series(chart, lv_palette_main(LV_PALETTE_RED), LV_CHART_AXIS_PRIMARY_Y);
    serPha = lv_chart_add_series(chart, lv_palette_main(LV_PALETTE_GREEN), LV_CHART_AXIS_PRIMARY_Y);
		
		// Button
		static lv_style_t button_style;
		lv_style_init(&button_style);
		lv_style_set_size(&button_style,20);
		lv_style_set_radius(&button_style, 30);
    lv_style_set_bg_opa(&button_style, LV_OPA_90);
    lv_style_set_bg_color(&button_style, lv_color_white());
    lv_style_set_border_width(&button_style, 1);
    lv_style_set_border_color(&button_style, lv_palette_darken(LV_PALETTE_GREY,5));
		lv_style_set_text_color(&button_style,lv_palette_darken(LV_PALETTE_GREY,5));
		
		lv_obj_t * label;
    btn_play = lv_btn_create(scr_main);
		lv_obj_add_style(btn_play, &button_style, 0);
    lv_obj_add_event_cb(btn_play, event_handler, LV_EVENT_CLICKED, NULL);
    lv_obj_align(btn_play, LV_ALIGN_BOTTOM_LEFT, 0, -20);
		// Button label
    btn_label = lv_label_create(btn_play);
    lv_label_set_text(btn_label, LV_SYMBOL_PLAY);
    lv_obj_center(btn_label);
		
		// Button
    btn_settings = lv_btn_create(scr_main);
		lv_obj_add_style(btn_settings, &button_style, 0);
    lv_obj_add_event_cb(btn_settings, event_handler, LV_EVENT_CLICKED, NULL);
    lv_obj_align(btn_settings, LV_ALIGN_BOTTOM_RIGHT, 0, -20);
		// Button label
    label = lv_label_create(btn_settings);
    lv_label_set_text(label, LV_SYMBOL_SETTINGS);
    lv_obj_center(label);
		
		// Button
    btn_switch = lv_btn_create(scr_main);
		lv_obj_add_style(btn_switch, &button_style, 0);
    lv_obj_add_event_cb(btn_switch, event_handler, LV_EVENT_CLICKED, NULL);
    lv_obj_align(btn_switch, LV_ALIGN_LEFT_MID, 0, 50);
		// Button label
    label = lv_label_create(btn_switch);
    lv_label_set_text(label, "S");
    lv_obj_center(label);

		// Button
    btn_plus = lv_btn_create(scr_main);
		lv_obj_add_style(btn_plus, &button_style, 0);
    lv_obj_add_event_cb(btn_plus, event_handler, LV_EVENT_CLICKED, NULL);
    lv_obj_align(btn_plus, LV_ALIGN_TOP_LEFT, 0, 20);
		// Button label
    label = lv_label_create(btn_plus);
    lv_label_set_text(label, LV_SYMBOL_PLUS);
    lv_obj_center(label);

		// Button
    btn_minus = lv_btn_create(scr_main);
		lv_obj_add_style(btn_minus, &button_style, 0);
    lv_obj_add_event_cb(btn_minus, event_handler, LV_EVENT_CLICKED, NULL);
    lv_obj_align(btn_minus, LV_ALIGN_TOP_LEFT, 0, 50);
		// Button label
    label = lv_label_create(btn_minus);
    lv_label_set_text(label, LV_SYMBOL_MINUS);
    lv_obj_center(label);
		
		lv_scr_load(scr_main);
		
		
		// Settings menu*******************************************************************
		scr_menu = lv_obj_create(NULL);
		
		/*Create a menu object*/
    menu = lv_menu_create(scr_menu);
    lv_color_t bg_color = lv_obj_get_style_bg_color(menu, 0);
    if(lv_color_brightness(bg_color) > 127) {
        lv_obj_set_style_bg_color(menu, lv_color_darken(lv_obj_get_style_bg_color(menu, 0), 10), 0);
    }
    else {
        lv_obj_set_style_bg_color(menu, lv_color_darken(lv_obj_get_style_bg_color(menu, 0), 50), 0);
    }
    lv_menu_set_mode_root_back_btn(menu, LV_MENU_ROOT_BACK_BTN_ENABLED);
    lv_obj_add_event_cb(menu, back_event_handler, LV_EVENT_CLICKED, menu);
    lv_obj_set_size(menu, lv_disp_get_hor_res(NULL), lv_disp_get_ver_res(NULL));
    lv_obj_center(menu);

    lv_obj_t * cont;
    lv_obj_t * section;

    /*Create sub pages*/
    lv_obj_t * sub_radar_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_radar_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    lv_menu_separator_create(sub_radar_page);
    section = lv_menu_section_create(sub_radar_page);
		label_list.fmin = create_label(section,LV_SYMBOL_SETTINGS,"Frequency min, MHz","600");
		label_list.fmax = create_label(section,LV_SYMBOL_SETTINGS,"Frequency max, MHz","3000");
		label_list.fstep = create_label(section,LV_SYMBOL_SETTINGS,"Frequency step, MHz","10");
		label_list.texp = create_label(section,LV_SYMBOL_SETTINGS,"Time exposition, ns","60");
		label_list.power = create_label(section,LV_SYMBOL_SETTINGS,"Power, dB","0");
		label_list.phase = create_label(section,LV_SYMBOL_SETTINGS,"Phase, deg","0");

    lv_obj_t * sub_single_shot_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_single_shot_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    lv_menu_separator_create(sub_single_shot_page);
    section = lv_menu_section_create(sub_single_shot_page);
		label_list.f = create_label(section,LV_SYMBOL_SETTINGS,"Frequency, MHz","1000");
		label_list.p = create_label(section,LV_SYMBOL_SETTINGS,"Power","0");
		lv_obj_t * obj = lv_menu_cont_create(section);
		lv_obj_t *btn_shot = lv_btn_create(obj);
		btn_label = lv_label_create(btn_shot);
		lv_label_set_text(btn_label,"Shot");

    lv_obj_t * sub_display_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_display_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    lv_menu_separator_create(sub_display_page);
    section = lv_menu_section_create(sub_display_page);
		label_list.nfft = create_label(section,LV_SYMBOL_SETTINGS,"N FFT, MHz","1000");
		label_list.window = create_label(section,LV_SYMBOL_SETTINGS,"Window","0");

		menu_set_values();
		
    lv_obj_t * sub_software_info_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_software_info_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    section = lv_menu_section_create(sub_software_info_page);
    create_text(section, NULL, "Version 1.0", LV_MENU_ITEM_BUILDER_VARIANT_1);

    lv_obj_t * sub_legal_info_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_legal_info_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    section = lv_menu_section_create(sub_legal_info_page);
    for(uint32_t i = 0; i < 15; i++) {
        create_text(section, NULL,
                    "This is a long long long long long long long long long text, if it is long enough it may scroll.",
                    LV_MENU_ITEM_BUILDER_VARIANT_1);
    }

    lv_obj_t * sub_about_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_about_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    lv_menu_separator_create(sub_about_page);
    section = lv_menu_section_create(sub_about_page);
    cont = create_text(section, NULL, "Software information", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_software_info_page);
    cont = create_text(section, NULL, "Legal information", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_legal_info_page);

    lv_obj_t * sub_menu_mode_page = lv_menu_page_create(menu, NULL);
    lv_obj_set_style_pad_hor(sub_menu_mode_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    lv_menu_separator_create(sub_menu_mode_page);
    section = lv_menu_section_create(sub_menu_mode_page);

    /*Create a root page*/
    root_page = lv_menu_page_create(menu, "Settings");
    lv_obj_set_style_pad_hor(root_page, lv_obj_get_style_pad_left(lv_menu_get_main_header(menu), 0), 0);
    section = lv_menu_section_create(root_page);
    cont = create_text(section, LV_SYMBOL_SETTINGS, "Radar", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_radar_page);
    cont = create_text(section, LV_SYMBOL_SETTINGS, "Single shot", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_single_shot_page);
    cont = create_text(section, LV_SYMBOL_SETTINGS, "Display", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_display_page);

    create_text(root_page, NULL, "Others", LV_MENU_ITEM_BUILDER_VARIANT_1);
    section = lv_menu_section_create(root_page);
    cont = create_text(section, NULL, "About", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_about_page);
    cont = create_text(section, LV_SYMBOL_SETTINGS, "Menu mode", LV_MENU_ITEM_BUILDER_VARIANT_1);
    lv_menu_set_load_page_event(menu, cont, sub_menu_mode_page);

    lv_menu_set_sidebar_page(menu, root_page);

    lv_event_send(lv_obj_get_child(lv_obj_get_child(lv_menu_get_cur_sidebar_page(menu), 0), 0), LV_EVENT_CLICKED, NULL);

		// Keyboard*******************************************************************
		ta = lv_textarea_create(scr_menu);
    lv_textarea_set_one_line(ta, true);
		lv_obj_set_width(ta, lv_pct(40));
    lv_obj_align(ta, LV_ALIGN_TOP_LEFT, 0, 10);
    lv_obj_add_event_cb(ta, textarea_event_handler, LV_EVENT_READY, ta);
    lv_obj_add_state(ta, LV_STATE_FOCUSED); /*To be sure the cursor is visible*/
		lv_obj_add_flag(ta,LV_OBJ_FLAG_HIDDEN);
		
		static const char * btnm_map[] = {"1", "2", "3", "\n",
                                      "4", "5", "6", "\n",
                                      "7", "8", "9", "\n",
                                      LV_SYMBOL_BACKSPACE, "0", LV_SYMBOL_NEW_LINE, ""
                                     };

    btnm = lv_btnmatrix_create(scr_menu);
    lv_obj_set_size(btnm, 200, 150);
    lv_obj_align(btnm, LV_ALIGN_BOTTOM_LEFT, 0, -10);
    lv_obj_add_event_cb(btnm, btnm_event_handler, LV_EVENT_VALUE_CHANGED, ta);
    lv_obj_clear_flag(btnm, LV_OBJ_FLAG_CLICK_FOCUSABLE); /*To keep the text area focused on button clicks*/
		lv_obj_add_flag(btnm,LV_OBJ_FLAG_HIDDEN);
    lv_btnmatrix_set_map(btnm, btnm_map);
}

static void btnm_event_handler(lv_event_t * e)
{
		lv_event_code_t code = lv_event_get_code(e);
    lv_obj_t * obj = lv_event_get_target(e);
		lv_obj_t * ta = lv_event_get_user_data(e);
    const char * txt = lv_btnmatrix_get_btn_text(obj, lv_btnmatrix_get_selected_btn(obj));

    if(strcmp(txt, LV_SYMBOL_BACKSPACE) == 0) lv_textarea_del_char(ta);
    else if(strcmp(txt, LV_SYMBOL_NEW_LINE) == 0) lv_event_send(ta, LV_EVENT_READY, NULL);
    else lv_textarea_add_text(ta, txt);
}
static void textarea_event_handler(lv_event_t * e)
{
    lv_obj_t * ta = lv_event_get_target(e);
		const char *txt = lv_textarea_get_text(ta);
		lv_label_set_text(cur_label,txt);
		menu_get_values();
			lv_obj_add_flag(btnm,LV_OBJ_FLAG_HIDDEN);
			lv_obj_add_flag(ta,LV_OBJ_FLAG_HIDDEN);
}

static void back_event_handler(lv_event_t * e)
{
    lv_obj_t * obj = lv_event_get_target(e);
    lv_obj_t * menu = lv_event_get_user_data(e);

    if(lv_menu_back_btn_is_root(menu, obj)) {
				lv_scr_load(scr_main);
    }
}

static void settings_event_handler(lv_event_t * e)
{
    cur_label = lv_event_get_target(e);
		lv_textarea_set_text(ta,lv_label_get_text(cur_label));
			lv_obj_clear_flag(btnm,LV_OBJ_FLAG_HIDDEN);
			lv_obj_clear_flag(ta,LV_OBJ_FLAG_HIDDEN);
}


static lv_obj_t * create_text(lv_obj_t * parent, const char * icon, const char * txt,
                              lv_menu_builder_variant_t builder_variant)
{
    lv_obj_t * obj = lv_menu_cont_create(parent);

    lv_obj_t * img = NULL;
    lv_obj_t * label = NULL;

    if(icon) {
        img = lv_img_create(obj);
        lv_img_set_src(img, icon);
    }

    if(txt) {
        label = lv_label_create(obj);
        lv_label_set_text(label, txt);
        lv_label_set_long_mode(label, LV_LABEL_LONG_SCROLL_CIRCULAR);
        lv_obj_set_flex_grow(label, 1);
    }

    if(builder_variant == LV_MENU_ITEM_BUILDER_VARIANT_2 && icon && txt) {
        lv_obj_add_flag(img, LV_OBJ_FLAG_FLEX_IN_NEW_TRACK);
        lv_obj_swap(img, label);
    }

    return obj;
}
static lv_obj_t * create_label(lv_obj_t * parent, const char * icon, const char * txt_left, const char * txt_right)
{
    lv_obj_t * obj = lv_menu_cont_create(parent);

    lv_obj_t * img = NULL;
    lv_obj_t * label = NULL;

    if(icon) {
        img = lv_img_create(obj);
        lv_img_set_src(img, icon);
    }

    if(txt_left) {
        label = lv_label_create(obj);
        lv_label_set_text(label, txt_left);
        lv_label_set_long_mode(label, LV_LABEL_LONG_SCROLL_CIRCULAR);
        lv_obj_set_flex_grow(label, 1);
    }

    if(txt_right) {
//      label = lv_textarea_create(obj);
//			lv_textarea_set_one_line(label,1);
//			lv_obj_set_width(label, lv_pct(30));
////			lv_obj_set_height(label, lv_pct(5));
//			lv_textarea_add_text(label, txt_right);

        label = lv_label_create(obj);
        lv_label_set_text(label, txt_right);
        lv_label_set_long_mode(label, LV_LABEL_LONG_SCROLL_CIRCULAR);
				lv_obj_align(label,LV_ALIGN_RIGHT_MID,0,0);
				lv_obj_set_style_align(label,LV_TEXT_ALIGN_RIGHT,0);
				lv_obj_add_flag(label,LV_OBJ_FLAG_CLICKABLE);
				lv_obj_add_event_cb(label,settings_event_handler,LV_EVENT_CLICKED, NULL);
//        lv_obj_set_flex_grow(label, 1);
    }

    return label;
}

//------------- Drivers ------------------------------------------
void my_disp_flush(lv_disp_drv_t * disp, const lv_area_t * area, lv_color_t * color_p)
{
    int32_t x, y;
	short* c;
    /*It's a very slow but simple implementation.
     *`set_pixel` needs to be written by you to a set pixel on the screen*/
	if(LVGL_UPDATE == LVGL_LOOP){
		for(y = area->y1; y <= area->y2; y++) {
			for(x = area->x1; x <= area->x2; x++) {
				c = (short*)color_p;
				ili9341_set_pixel(x, y, *c);
				color_p++;
			}
		}
	}
	else if(LVGL_UPDATE == LVGL_MATRIX){
		ili9341_set_area(area->x1, area->x2
			,area->y1, area->y2);
		ili9341_set_value((short*)color_p,(area->x2-area->x1+1)*(area->y2-area->y1+1));
	}
	else if(LVGL_UPDATE == LVGL_DMA){
		ili9341_set_area(area->x1, area->x2
			,area->y1, area->y2);
		ili9341_dma_send((short*)color_p,(area->x2-area->x1+1)*(area->y2-area->y1+1));
		ili9341_dma_wait();
	}

    lv_disp_flush_ready(disp);         /* Indicate you are ready with the flushing*/
}

void my_touchpad_read(struct _lv_indev_drv_t * indev, lv_indev_data_t * data)
{
	int xy;
    /*`touchpad_is_pressed` and `touchpad_get_xy` needs to be implemented by you*/
    if(xpt2046_is_pressed()) {
      data->state = LV_INDEV_STATE_PRESSED;
		xy = xpt2046_xy();
		data->point.x = xy>>16;
		data->point.y = xy&0xFFFF;
    } else {
      data->state = LV_INDEV_STATE_RELEASED;
    }

}
