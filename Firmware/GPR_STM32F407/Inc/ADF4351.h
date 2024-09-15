// ADF4351 board pins

//	CLK
//Serial Clock Input. Data is clocked into the 32-bit shift register on the CLK rising edge. This input is a high
//impedance CMOS input.
//	DATA
//Serial Data Input. The serial data is loaded MSB first with the three LSBs as the control bits. This input is a high
//impedance CMOS input.
//	LE
//Load Enable. When LE goes high, the data stored in the 32-bit shift register is loaded into the register that is
//selected by the three control bits. This input is a high impedance CMOS input.
//	CE
//Chip Enable. A logic low on this pin powers down the device and puts the charge pump into three-state mode.
//A logic high on this pin powers up the device, depending on the status of the power-down bits.
//	PDB RF
//RF Power-Down. A logic low on this pin mutes the RF outputs. This function is also software controllable.
//	MUXOUT
//Multiplexer Output. The multiplexer output allows the lock detect value, the N divider value, or the R counter
//value to be accessed externally.
//	LD
//Lock Detect Output Pin. A logic high output on this pin indicates PLL lock. A logic low output indicates loss
//of PLL lock.

// Prototypes
void adf4351_init_mcu(void);
void adf4351_deinit_mcu(void);
void adf4351_init(void);
char adf4351_set_frequency(int f);
void adf4351_set_power(short gain);
char adf4351_read_lock();

// Register definition. Fields position
#define ADF4351_REG 0
// ---------------- R0 ----------------------
#define ADF4351_FRAC 3	//12-Bit Fractional Value (FRAC): 1..4095
#define ADF4351_INT 15	//16-Bit Integer Value (INT): 23..65535
// ------------------ R1 -------------------------
#define ADF4351_MOD 3	//12-Bit Modulus Value (MOD): 2..4095
#define ADF4351_PHASE 15	//12-Bit Phase Value: 0..4095, 1 - recommended
#define ADF4351_PRESC 27	//Prescaler Value: 0 - 4/5, 1 - 5/9
//- Prescaler = 4/5: N MIN = 23
//- Prescaler = 8/9: N MIN = 75
#define ADF4351_PHADJ 28	//Phase Adjust: 0 - OFF, 1 - ON
// ------------------ R2 -----------------------
#define ADF4351_CNTRES 3	//Counter Reset: 0 - DISABLED, 1 - ENABLED
#define ADF4351_CP3ST 4	//Charge Pump Three-State: 0 - DISABLED, 1 - ENABLED
#define ADF4351_POWDOWN 5	//Power-Down (PD): 0 - DISABLED, 1 - ENABLED
#define ADF4351_PDPOL 6	//Phase Detector Polarity: 0 - NEGATIVE, 1 - POSITIVE
#define ADF4351_LDP 7	//Lock Detect Precision (LDP): 0 - 10ns, 1 - 6ns
#define ADF4351_LDF 8	//Lock Detect Function (LDF): 0 - FRAC-N, 1 - INT-N
#define ADF4351_CPCURSET 9	//Charge Pump Current Setting: 0 - 0.31, 1 - 0.63
//		2 - 0.94, 3 - 1.25, 4 - 1.56, 5 - 1.88, 6 - 2.19
//		7 - 2.50, 8 - 2.81, 9 - 3.13, 10 - 3.44, 11 - 3.75
//		12 - 4.06, 13 - 4.38, 14 - 4.69, 15 - 5.00
#define ADF4351_DBLBUF 13	//Double Buffer: 0 - DISABLED, 1 - ENABLED
#define ADF4351_RCNT 14	//10-Bit R Counter: 1..1023
#define ADF4351_RDIV2 24	//RDIV2: 0 - DISABLED, 1 - ENABLED
#define ADF4351_REFDBLR 25	//Reference Doubler: 0 - DISABLED, 1 - ENABLED
#define ADF4351_MUXOUT 26	//MUXOUT: 0 - THREE-STATE OUTPUT, 1 - DV DD
//		2 - DGND, 3 - R COUNTER OUTPUT, 4 - N DIVIDER OUTPUT
//		5 - ANALOG LOCK DETECT,6 - DIGITAL LOCK DETECT
#define ADF4351_LNMOD 29	//Low Noise and Low Spur Modes: 
//		0 - LOW NOISE MODE, 3 - LOW SPUR MODE
// ---------------- R3 --------------------
#define ADF4351_CLKDIV 3	//12-Bit Clock Divider Value: 1..4095
#define ADF4351_DIVMOD 15	//Clock Divider Mode: 0 - CLOCK DIVIDER OFF, 1 - FAST LOCK ENABLE
//		2 - RESYNC ENABLE
#define ADF4351_CSR 18	//CSR Enable: 0 - DISABLED, 1 - ENABLED
#define ADF4351_CHRGCNCL 21	//Charge Cancelation: 0 - DISABLED, 1 - ENABLED
#define ADF4351_ABP 22	//Antibacklash Pulse Width (ABP): 0 - 6ns (FRAC-N), 1 - 3ns (INT-N)
#define ADF4351_BNDSEL 23	//Band Select Clock Mode: 0 - LOW, 1 - HIGH
// ------------------ R4 ---------------------------
#define ADF4351_OUTPOW 3		//Output Power: 0 - -4dBm, 1 - -1dBm, 2 - +2dBm, 3 - +5dBm
#define ADF4351_RFOUTEN 5		//RF Output Enable: 0 - DISABLED, 1 - ENABLED
#define ADF4351_AUXOUTPOW 6		//AUX Output Power: 0 - -4dBm, 1 - -1dBm, 2 - +2dBm, 3 - +5dBm
#define ADF4351_AUXOUTEN 8		//AUX Output Enable: 0 - DISABLED, 1 - ENABLED
#define ADF4351_AUXOUTSEL 9		//AUX Output Select: 0 - DIVIDED, 1 - FUNDAMENTAL
#define ADF4351_MTLD 10		//Mute Till Lock Detect (MTLD): 0 - DISABLED, 1 - ENABLED
#define ADF4351_VCOPD 11		//VCO Power-Down: 0 - VCO POWERED UP, 1 - VCO POWERED DOWN
#define ADF4351_BNDSELCLKDIV 12	//Band Select Clock Divider Value: 1..255
#define ADF4351_RFDIVSEL 20		//RF Divider Select: 0 - /1 .. 6 - /64
#define ADF4351_FDBKSEL 23		//Feedback Select: 0 - DIVIDED, 1 - FUNDAMENTAL
// ----------------- R5 ------------------------
#define bR5 0x180000
#define ADF4351_LDPINMOD 22	//Lock Detect Pin Operation
// 		0 - LOW
// 		1 - DIGITAL LOCK DETECT
// 		2 - LOW
// 		3 - HIGH

struct ADF4351_reg_struct{
	struct R0_struct{
		unsigned int CTRL:3;
		unsigned int FRAC:12;
		unsigned int INT:16;
		unsigned int reserve1:1;
	} R0;
	struct R1_struct{
		unsigned int CTRL:3;
		unsigned int MOD:12;
		unsigned int PHASE:12;
		unsigned int PRESCALER:1;
		unsigned int PHASE_ADJUST:1;
		unsigned int reserve2:3;
	} R1;
	struct R2_struct{
		unsigned int CTRL:3;
		unsigned int COUNTER_RESET:1;
		unsigned int CP_THREE_STATE:1;
		unsigned int POWER_DOWN:1;
		unsigned int PD_POLARITY:1;
		unsigned int LDP:1;
		unsigned int LDF:1;
		unsigned int CHARGE_PUMP_CURRENT_SETTING:4;
		unsigned int DOUBLE_BUFFER:1;
		unsigned int R_COUNTER:10;
		unsigned int RVID2:1;
		unsigned int REFERENCE_DOUBLER:1;
		unsigned int MUXOUT:3;
		unsigned int LOW_NOISE_AD_LOW_SPUR_MODE:2;
		unsigned int reserve3:1;
	} R2;
	struct R3_struct{
		unsigned int CTRL:3;
		unsigned int CLOCK_DIVIDER_VALUE:12;
		unsigned int CLK_DIV_MODE:2;
		unsigned int reserve4:1;
		unsigned int CSR:1;
		unsigned int reserve5:2;
		unsigned int CHARGE_CANCEL:1;
		unsigned int APB:1;
		unsigned int BAND_SELECT_CLOCK_MODE:1;
		unsigned int reserve6:8;
	} R3;
	struct R4_struct{
		unsigned int CTRL:3;
		unsigned int OUTPUT_POWER:2;
		unsigned int RF_OUTPUT_ENABLE:1;
		unsigned int AUX_OUTPUT_POWER:2;
		unsigned int AUX_OUTPUT_ENABLE:1;
		unsigned int AUX_OUTPUT_SELECT:1;
		unsigned int MTLD:1;
		unsigned int VCO_POWER_DOWN:1;
		unsigned int BAND_SELECT_CLOCK_DIVIDER_VALUE:8;
		unsigned int RF_DIVIDER_SELECT:3;
		unsigned int FEEDBACK_SELECT:1;
		unsigned int reserve7:8;
	} R4;
	struct R5_struct{
		unsigned int CTRL:3;
		unsigned int reserve8:19;
		unsigned int LD_PIN_MODE:2;
		unsigned int reserve9:8;
	} r5;
};
//	REGISTER INITIALIZATION SEQUENCE
//At initial power-up, after the correct application of voltages to
//the supply pins, the ADF4351 registers should be started in the
//following sequence:
//1. Register 5
//2. Register 4
//3. Register 3
//4. Register 2
//5. Register 1
//6. Register 0