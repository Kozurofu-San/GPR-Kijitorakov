#pragma once

#include <math.h>

//extern constexpr int FFT_N_MAX;
#define FFT_N_MAX (1*1024)

void fft_init(const int n);
void fft_transform(float inRe[], float inIm[], int len,
    float outRe[], float outIm[]);
void fft_transform_inverse(float inRe[], float inIm[], int len, float outRe[], float outIm[]);
void hilbert(float inRe[], float inIm[], int len,float tempRe[],float tempIm[]);

void fft_q16_init(const int n);
void fft_q16_transform(int inRe[], int inIm[], int len,
    int outRe[], int outIm[]);

double sin7(double x);
inline double cos7(double x);
float sin7f(float x);
inline float cos7f(float x);
#define Q16_PI 0x0003243F
#define Q16_PI_2 0x00019220
int sin7q16(int x);
inline int cos7q16(int x);

float absf(float re, float im);
int abs_q16(int re, int im);
float rsqrt(float number);
int sqrt_q16(int x);
int mul_q16(int a, int b);
int q16_to_int(int a);
inline int int_to_q16(int a);

///* The basic operations perfomed on two numbers a and b of fixed
// point q format returning the answer in q format */
//#define FADD(a,b) ((a)+(b))
//#define FSUB(a,b) ((a)-(b))
//#define FMUL(a,b,q) ((((long long)a)*((long long)b))>>(q))
//#define FDIV(a,b,q) (((a)<<(q))/(b))
// /* The basic operations where a is of fixed point q format and b is
//  an integer */
//#define FADDI(a,b,q) ((a)+((b)<<(q)))
//#define FSUBI(a,b,q) ((a)-((b)<<(q)))
//#define FMULI(a,b) ((a)*(b))
//#define FDIVI(a,b) ((a)/(b))
//  /* convert a from q1 format to q2 format */
//#define FCONV(a, q1, q2) (((q2)>(q1)) ? (a)<<((q2)-(q1)) : (a)>>((q1)-(q2)))
///* the general operation between a in q1 format and b in q2 format
// returning the result in q3 format */
//#define FADDG(a,b,q1,q2,q3) (FCONV(a,q1,q3)+FCONV(b,q2,q3))
//#define FSUBG(a,b,q1,q2,q3) (FCONV(a,q1,q3)-FCONV(b,q2,q3))
//#define FMULG(a,b,q1,q2,q3) FCONV((a)*(b), (q1)+(q2), q3)
//#define FDIVG(a,b,q1,q2,q3) (FCONV(a, q1, (q2)+(q3))/(b))
// /* convert to and from floating point */
//#define TOFIX(d, q) ((int)( (d)*(double)(1<<(q)) ))
//#define TOFLT(a, q) ( (double)(a) / (double)(1<<(q)) )
//	

// Define _USE_MATH_DEFINES before including <math.h> to expose these macro
// definitions for common math constants.  These are placed under an #ifdef
// since these commonly-defined names are not part of the C or C++ standards
#define M_E        2.71828182845904523536   // e
#define M_LOG2E    1.44269504088896340736   // log2(e)
#define M_LOG10E   0.434294481903251827651  // log10(e)
#define M_LN2      0.693147180559945309417  // ln(2)
#define M_LN10     2.30258509299404568402   // ln(10)
#define M_PI       3.14159265358979323846   // pi
#define M_PI_2     1.57079632679489661923   // pi/2
#define M_PI_4     0.785398163397448309616  // pi/4
#define M_1_PI     0.318309886183790671538  // 1/pi
#define M_2_PI     0.636619772367581343076  // 2/pi
#define M_2_SQRTPI 1.12837916709551257390   // 2/sqrt(pi)
#define M_SQRT2    1.41421356237309504880   // sqrt(2)
#define M_SQRT1_2  0.707106781186547524401  // 1/sqrt(2)
