// AD9910 definitions

// SDIO
// Serial Data Input/Output. Digital input/output (active high). This pin can be either unidirec-
// tional or bidirectional (default), depending on the configuration settings. In bidirectional serial
// port mode, this pin acts as the serial data input and output. In unidirectional mode, it is an
// input only.
// SDO
// Serial Data Output. Digital output (active high). This pin is only active in unidirectional
// serial data mode. In this mode, it functions as the output. In bidirectional mode, this pin is
// not operational and should be left floating.
// SCLK
// Serial Data Clock. Digital clock (rising edge on write, falling edge on read). This pin provides
// the serial data clock for the control data path. Write operations to the AD9910 use the
// rising edge. Readback operations from the AD9910 use the falling edge.
// CS
// Chip Select. Digital input (active low). This pin allows the AD9910 to operate on a common
// serial bus for the control data path. Bringing this pin low enables the AD9910 to detect
// serial clock rising/falling edges. Bringing this pin high causes the AD9910 to ignore input
// on the serial data pins.
// I/O_RESET
// Input/Output Reset. Digital input (active high). This pin can be used when a serial I/O
// communication cycle fails (see the I/O_RESET—Input/Output Reset section for details).
// When not used, connect this pin to ground.
// I/O_UPDATE
// Input/Output Update. Digital input (active high). A high on this pin transfers the contents
// of the I/O buffers to the corresponding internal registers.
// EXT_PWR_DWN
// External Power-Down, Digital Input (Active High). A high level on this pin initiates the
// currently programmed power-down mode. See the Power-Down Control section for
// further details. If unused, connect to ground.

// Prototypes
void ad9910_init_mcu(void);
void ad9910_deinit_mcu(void);
void ad9910_init(void);
void ad9910_power(short gain);
char ad9910_set_frequency(int f);

// Control Function Register 1
#define AD9910_CFR1 0x00
#define AD9910_LSB_first 0
#define AD9910_SDIO_inpit_only 1
#define AD9910_External_power_down_control 3
#define AD9910_Aux_DAC_power_down 4
#define AD9910_REFCLK_input_power_down 5
#define AD9910_DAC_power_down 6
#define AD9910_Digital_power_down 7
#define AD9910_Select_auto_OSK 8
#define AD9910_OSK_enable 9
#define AD9910_Load_ARR_IO_update 10
#define AD9910_Clear_phase_accumulator 11
#define AD9910_Clear_digital_ramp_accumulator 12
#define AD9910_Autoclear_phase_accumulator 13
#define AD9910_Autoclear_digital_ramp_accumulator 14
#define AD9910_Load_LRR_IO_update 15
#define AD9910_Select_DDS_sine_output 16
#define AD9910_Internal_profile_control 17
#define AD9910_Inverse_sinc_filter_enable 22
#define AD9910_Manual_OSK_external_control 23
#define AD9910_RAM_playback_destination 29 
#define AD9910_RAM_enable 31
// Control Function Register 2
#define AD9910_CFR2 0x01
#define AD9910_FM_gain 0
#define AD9910_Parallel_data_port_enable 4
#define AD9910_Sync_timing_validation_disable 5
#define AD9910_Data_assembler_hold_last_value 6
#define AD9910_Matched_latency_enable 7
#define AD9910_TxEnable_invert 9
#define AD9910_PDCLK_invert 10
#define AD9910_PDCLK_enable 11
#define AD9910_IO_update_rate_control 14
#define AD9910_Read_effective_FTW 16
#define AD9910_Digital_ramp_no_dwell_low 17
#define AD9910_Digital_ramp_no_dwell_high 18
#define AD9910_Digital_ramp_enable 19
#define AD9910_Digital_ramp_destination 20
#define AD9910_SYNC_CLK_enable 22
#define AD9910_Internal_IO_update_active 23
#define AD9910_Enable_amplitude_scale_from_single_tone_profiles 24
// Control Function Register 3
#define AD9910_CFR3 0x02
#define AD9910_CFR3_ 0x08070000
#define AD9910_N 1
#define AD9910_PLL_enable 8
#define AD9910_PFD_reset 10
#define AD9910_REFCLK_input_divider_ResetB 14
#define AD9910_REFCLK_input_divider_bypass 15
#define AD9910_I_CP 19
#define AD9910_VCO_SEL 24
#define AD9910_DRV0 28
// Auxiliary DAC Control Register
#define AD9910_Auxiliary_DAC_Control 0x03
#define AD9910_FSC 0
// I/O Update Rate Register
#define AD9910_IO_Update_Rate 0x04
#define AD9910_IO_update_rate 0
// Frequency Tuning Word Register
#define AD9910_FTW 0x07
#define AD9910_Frequency_tuning_word 0
// Phase Offset Word Register
#define AD9910_POW 0x08
#define AD9910_Phase_offset_word 0
// Amplitude Scale Factor Register
#define AD9910_ASF 0x09
#define AD9910_Amplitude_step_size 0
#define AD9910_Amplitude_scale_factor 2
#define AD9910_Amplitude_ramp_rate 16
// Multichip Sync Register
#define AD9910_Multichip_Sync 0x0A
#define AD9910_Input_sync_receiver_delay 3
#define AD9910_Output_sync_generator_delay 11
#define AD9910_Sync_state_preset_value 18
#define AD9910_Sync_generator_polarity 25
#define AD9910_Sync_generator_enable 26
#define AD9910_Sync_receiver_enable 27
#define AD9910_Sync_validation_delay 28
// Digital Ramp Limit Register
#define AD9910_Digital_Ramp_Limit 0x0B
#define AD9910_Digital_ramp_lower_limit 0
#define AD9910_Digital_ramp_upper_limit 32
// Digital Ramp Step Size Register
#define AD9910_Digital_Ramp_Step_Size 0x0C
#define AD9910_Digital_ramp_increment_step_size 0
#define AD9910_Digital_ramp_decrement_step_size 32
// Digital Ramp Rate Register
#define AD9910_Digital_Ramp_Rate 0x0D
#define AD9910_Digital_ramp_positive_slope_rate 0
#define AD9910_Digital_ramp_negative_slope_rate 16
// Single Tone Profile n, n=0..7
#define AD9910_Single_Tone_Profile 0x0E
#define AD9910_Frequency_Tuning_Word 0
#define AD9910_Phase_Offset_Word 32
#define AD9910_Amplitude_Scale_Factor 48
// RAM Profile n, n=0..7
#define AD9910_RAM_Profile 0x0E
#define AD9910_RAM_Profile_mode_control 0
#define AD9910_Zero_crossing 3
#define AD9910_No_dwell_high 5
#define AD9910_RAM_Profile_waveform_start_addres 14
#define AD9910_RAM_Profile_waveform_end_address 30
#define AD9910_RAM_Profile_address_step_rate 40
// RAM Register
#define AD9910_RAM 0x16
#define AD9910_RAM_word 0

struct AD9910_reg_struct{
	struct CFR1_struct{
		unsigned int LSB_first:1;
		unsigned int SDIO_inpit_only:1;
		unsigned int reserved0:1;
		unsigned int External_power_down_control:1;
		unsigned int Aux_DAC_power_down:1;
		unsigned int REFCLK_input_power_down:1;
		unsigned int DAC_power_down:1;
		unsigned int Digital_power_down:1;
		unsigned int Select_auto_OSK:1;
		unsigned int OSK_enable:1;
		unsigned int Load_ARR_IO_update:1;
		unsigned int Clear_phase_accumulator:1;
		unsigned int Clear_digital_ramp_accumulator:1;
		unsigned int Autoclear_phase_accumulator:1;
		unsigned int Autoclear_digital_ramp_accumulator:1;
		unsigned int Load_LRR_IO_update:1;
		unsigned int Select_DDS_sine_output:1;
		unsigned int internal_profile_control:4;
		unsigned int Inverse_sinc_filter_enable:1;
		unsigned int Manual_OSK_external_control:1;
		unsigned int reserved1:5;
		unsigned int RAM_playback_destination:2;
		unsigned int RAM_enable:1;
	} CFR1;
	struct CFR2_struct{
		unsigned int FM_gain:4;
		unsigned int Parallel_data_port_enable:1;
		unsigned int Sync_timing_validation_disable:1;
		unsigned int Data_assembler_hold_last_value:1;
		unsigned int Matched_latency_enable:1;
		unsigned int reserved0:1;
		unsigned int TxEnable_invert:1;
		unsigned int PDCLK_invert:1;
		unsigned int PDCLK_enable:1;
		unsigned int reserved1:2;
		unsigned int IO_update_rate_control:2;
		unsigned int Read_effective_FTW:1;
		unsigned int Digital_ramp_no_dwell_low:1;
		unsigned int Digital_ramp_no_dwell_high:1;
		unsigned int Digital_ramp_enable:1;
		unsigned int Digital_ramp_destination:1;
		unsigned int SYNC_CLK_enable:1;
		unsigned int internal_IO_update_active:1;
		unsigned int Enable_amplitude_scale_from_single_tone_profiles:1;
		unsigned int reserved2:7;
	} CFR2;
	struct CFR3_struct{
		unsigned int reserved0:1;
		unsigned int N:7;
		unsigned int PLL_enable:1;
		unsigned int reserved1:1;
		unsigned int PFD_reset:1;
		unsigned int reserved2:3;
		unsigned int REFCLK_input_divider_ResetB:1;
		unsigned int REFCLK_input_divider_bypass:1;
		unsigned int reserved3:3;
		unsigned int I_CP:3;
		unsigned int reserved4:2;
		unsigned int VCO_SEL:3;
		unsigned int reserved5:1;
		unsigned int DRV0:2;
		unsigned int reserved6:2;
	} CFR3;
	struct Auxiliary_DAC_Control_struct{
		unsigned int FSC:8;
		unsigned int reserved0:24;
	} Auxiliary_DAC_Control;
	struct IO_Update_Rate_struct{
		unsigned int IO_update_rate:32;
	} IO_Update_Rate;
	struct FTW_struct{
		unsigned int Frequency_tuning_word:32;
	} FTW;
	struct POW_struct{
		unsigned short Phase_offset_word:16;
	} POW;
	struct ASF_struct{
		unsigned int Amplitude_step_size:2;
		unsigned int Amplitude_scale_factor:14;
		unsigned int Amplitude_ramp_rate:16;
	} ASF;
	struct Multichip_Sync_struct{
		unsigned int reserved0:3;
		unsigned int Input_sync_receiver_delay:5;
		unsigned int reserved1:3;
		unsigned int Output_sync_generator_delay:5;
		unsigned int reserved2:2;
		unsigned int Sync_state_preset_value:6;
		unsigned int reserved3:1;
		unsigned int Sync_generator_polarity:1;
		unsigned int Sync_generator_enable:1;
		unsigned int Sync_receiver_enable:1;
		unsigned int Sync_validation_delay:4;
	} Multichip_Sync;
	struct Digital_Ramp_Limit_struct{
		unsigned long long Digital_ramp_lower_limit:32;
		unsigned long long Digital_ramp_upper_limit:32;
	} Digital_Ramp_Limit;
	struct Digital_Ramp_Step_Size_struct{
		unsigned long long Digital_ramp_increment_step_size:32;
		unsigned long long Digital_ramp_decrement_step_size:32;
	} Digital_Ramp_Step_Size;
	struct Digital_Ramp_Rate_struct{
		unsigned int Digital_ramp_positive_slope_rate:16;
		unsigned int Digital_ramp_negative_slope_rate:16;
	} Digital_Ramp_Rate;
	struct Single_Tone_Profile_struct{
		unsigned long long Frequency_Tuning_Word:32;
		unsigned long long Phase_Offset_Word:16;
		unsigned long long Amplitude_Scale_Factor:14;
		unsigned long long reserved0:2;
	} Single_Tone_Profile[8];
	struct RAM_Profile_struct{
		unsigned long long RAM_Profile_mode_control:3;
		unsigned long long Zero_crossing:1;
		unsigned long long reserved0:1;
		unsigned long long No_dwell_high:1;
		unsigned long long reserved1:8;
		unsigned long long RAM_Profile_waveform_start_addres:10;
		unsigned long long reserved:6;
		unsigned long long RAM_Profile_waveform_end_address:10;
		unsigned long long RAM_Profile_address_step_rate:10;
	} RAM_Profile[8];
	struct RAM_struct{
		unsigned int RAM_word:32;
	} RAM;
};
