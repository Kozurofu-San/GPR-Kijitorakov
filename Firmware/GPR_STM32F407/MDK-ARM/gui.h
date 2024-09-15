#pragma once

void gui_init();
void set_buttons_visible(char);
void update_gui();
void set_x_limits(uint32_t min, uint32_t max, uint32_t step);
void set_y_limits(int min, int max);
void gui_button_play_callback();
void gui_btn_switch_callback();
void gui_btn_switch_callback();
void set_data_re(short* data);
void set_data_im(short* data);


#define SCREEN_MAIN 21
#define SCREEN_SPECTER 22
#define SCREEN_FFT 23
extern char cur_scr;
