#include <stdlib.h>
#include <stdint.h>

#include "mathematics.h"
#include "functions.h"

unsigned short iR[FFT_N_MAX], curN = FFT_N_MAX;
int i, j, n, step, x, k, cnt, i0, i1;

float wRe[FFT_N_MAX], wIm[FFT_N_MAX];

void fft_init(int n) {
		curN = n;
    int N = n;
    int logN = -1;
    while (N > 0)
    {
        logN++;
        N /= 2;
    }
    for (int i = 0; i < curN; i++) {
        // Twiddle factors
        if (i < curN / 2)
        {
            wRe[i] = cos7f(2 * (float)M_PI * i / curN);
            wIm[i] = -sin7f(2 * (float)M_PI * i / curN);
        }
        // Shuffle indices
        int r = 0, p = i;
        for (int l = 0; l < logN; l++) {
            r <<= 1;
            r |= p & 1;
            p >>= 1;
        }
        iR[i] = r;
    }
}

void fft_transform(float inRe[], float inIm[], int len,
    float outRe[], float outIm[]) {
    static float evenRe, evenIm, oddRe, oddIm;
    int N = curN;
    for (i = 0; i < curN; i++) {
        outRe[i] = 0;
        outIm[i] = 0;
    }
    n = len;
    for (i = 0; i < curN; i++) {
        j = iR[i];
        if (j < n) {
            outRe[i] = inRe[j];
            outIm[i] = inIm[j];
        }
    }
    n = 2;
    step = N / 2;
    while (n <= N)
    {
        x = 0;
        for (cnt = 0; cnt < N / n; cnt++) {
            k = 0;
            for (i = 0; i < n / 2; i++) {
                i0 = (x + i);
                i1 = (x + i + n / 2);
                evenRe = outRe[i0];
                evenIm = outIm[i0];
                oddRe = wRe[k] * outRe[i1] - wIm[k] * outIm[i1];
                oddIm = wRe[k] * outIm[i1] + wIm[k] * outRe[i1];
                outRe[i0] = evenRe + oddRe;
                outIm[i0] = evenIm + oddIm;
                outRe[i1] = evenRe - oddRe;
                outIm[i1] = evenIm - oddIm;
                k += step;
            }
            x += n;
        }
        step /= 2;
        n *= 2;
    }
}
		
void fft_transform_inverse(float inRe[], float inIm[], int len, float outRe[], float outIm[]){
		for (int i=0; i<len; i++)
				inIm[i] = -inIm[i];
		fft_transform(inRe,inIm,len,outRe,outIm);
		for (int i=0; i<len; i++)
				inIm[i] = -inIm[i];
		for (int i=0; i<curN; i++) {
				outRe[i] /= (float)curN;
				outIm[i] = -outIm[i] /(float)curN;
		}
}

float tempRe_[FFT_N_MAX], tempIm_[FFT_N_MAX];
void hilbert(float inRe[], float inIm[], int len,float tempRe[],float tempIm[]){
		for(int i=0; i<len; i++)
			inIm[i] = 0;
		fft_transform(inRe,inIm,len,tempRe,tempIm);
		int i;
		for (i=1; i<curN/2; i++) {
			tempRe[i] = 2 * tempRe[i];
			tempIm[i] = 2 * tempIm[i];
		}
		for (i=curN/2+1; i<curN; i++) {
			tempRe[i] = 0;
			tempIm[i] = 0;
		}
		fft_transform_inverse(tempRe,tempIm,curN,tempRe_,tempIm_);
		for (i=0; i<len; i++)
				inIm[i] = tempIm_[i];
}

int wRe_q16[FFT_N_MAX], wIm_q16[FFT_N_MAX];

void fft_q16_init(const int n)
{
    int N = FFT_N_MAX;
    int logN = -1;
    while (N > 0)
    {
        logN++;
        N /= 2;
    }
    for (int i = 0; i < FFT_N_MAX; i++) {
        // Twiddle factors
        if (i < FFT_N_MAX / 2)
        {
            int arg = (2*Q16_PI*i) / FFT_N_MAX;
            wRe_q16[i] = cos7q16(arg);
            wIm_q16[i] = -sin7q16(arg);
        }
        // Shuffle indices
        int r = 0, p = i;
        for (int l = 0; l < logN; l++) {
            r <<= 1;
            r |= p & 1;
            p >>= 1;
        }
        iR[i] = r;
    }
}

int mul_q16(int a, int b) {
    long long temp = (long long)a * (long long)b;
    if ((temp & 0x8000) == 0x8000)
        temp += 0x10000;
    temp >>= 16;
		return (int)temp;
}

int q16_to_int(int a) {
    if ((a & 0x8000) == 0x8000)
        a += 0x10000;
    a >>= 16;
    return a;
}

inline int int_to_q16(int a){
    return a << 16;
}

void fft_q16_transform(int inRe[], int inIm[], int len, int outRe[], int outIm[])
{
    int N = FFT_N_MAX;
    static long long evenRe, evenIm, oddRe, oddIm;
    for (i = 0; i < FFT_N_MAX; i++) {
        outRe[i] = 0;
        outIm[i] = 0;
    }
    n = len;
    for (i = 0; i < FFT_N_MAX; i++) {
        j = iR[i];
        if (j < n) {
            outRe[i] = inRe[j];
            outIm[i] = inIm[j];
        }
    }
    n = 2;
    step = N / 2;
    while (n <= N)
    {
        x = 0;
        for (cnt = 0; cnt < N / n; cnt++) {
            k = 0;
            for (i = 0; i < n / 2; i++) {
                i0 = (x + i);
                i1 = (x + i + n / 2);
                if (i1 == 51)
                    i0 = i0;
                evenRe = outRe[i0];
                evenIm = outIm[i0];
                oddRe = mul_q16(wRe_q16[k], outRe[i1]) - mul_q16(wIm_q16[k], outIm[i1]);
                oddIm = mul_q16(wRe_q16[k], outIm[i1]) + mul_q16(wIm_q16[k], outRe[i1]);
                outRe[i0] = evenRe + oddRe;
                outIm[i0] = evenIm + oddIm;
                outRe[i1] = evenRe - oddRe;
                outIm[i1] = evenIm - oddIm;
                k += step;
            }
            x += n;
        }
        step /= 2;
        n *= 2;
    }
}

float absf(float re, float im) {
    return sqrt(re * re + im * im);
}

int abs_q16(int re, int im) {
    return sqrt_q16(mul_q16(re,re)+mul_q16(im,im));
}

double sin7(double x)
{
    x *= 0.63661977236758134308; // 2/Pi
    unsigned char sign = x < 0.0;
    if (sign)
        x = -x;
    int xf = (int)x;
    x -= xf;
    if ((xf & 1) == 1)
        x = 1 - x;
    unsigned  per = ((xf >> 1) & 1) == 1;
    double xx = x * x;
    double y = x * (1.5707903005870776 + xx * (-0.6458858977085938 +
        xx * (0.07941798513358536 - 0.0043223880120647346 * xx)));
    if (sign ^ per)
        y = -y;
    return y;
}

inline double cos7(double x) {
    return sin7(x + M_PI_2);
}

float sin7f(float x)
{
    x *= 0.63661977236758134308f; // 2/Pi
    unsigned char sign = x < 0.0;
    if (sign)
        x = -x;
    int xf = (int)x;
    x -= xf;
    if ((xf & 1) == 1)
        x = 1 - x;
    unsigned  per = ((xf >> 1) & 1) == 1;
    float xx = x * x;
    float y = x * (1.5707903005870776f + xx * (-0.6458858977085938f +
        xx * (0.07941798513358536f - 0.0043223880120647346f * xx)));
    if (sign ^ per)
        y = -y;
    return y;
}

inline float cos7f(float x) {
    return sin7f(x+(float)M_PI_2);
}

float rsqrt(float number)
{
    const float x2 = number * 0.5F;
    const float threehalfs = 1.5F;

    union {
        float f;
        uint32_t i;
    } conv = { number }; // member 'f' set to value of 'number'.
    conv.i = 0x5f3759df - (conv.i >> 1);
    conv.f *= threehalfs - x2 * conv.f * conv.f;
    return conv.f;
}

int sqrt_q16(int x) {
    uint32_t t, q, b, r;
    r = x;
    b = 0x40000000;
    q = 0;
    while (b > 0x40)
    {
        t = q + b;
        if (r >= t)
        {
            r -= t;
            q = t + b; // equivalent to q += 2*b
        }
        r <<= 1;
        b >>= 1;
    }
    q >>= 8;
    return q;
}

int sin7q16(int x) {
    x = mul_q16(x, 0x000000000000A2FA);
    unsigned char sign = x < 0;
    if (sign)
        x = -x;
    int xf = x & 0xFFFF0000;
    x -= xf;
    if (xf & 0x00010000)
        x = 0x00010000 - x;
    unsigned char per = ((xf >> 1) & 0x00010000) == 0x00010000;
    int xx = mul_q16(x, x);
    long long a = (int)(-283);
    int y = mul_q16(xx, 0xFFFFFEE5);
    y += 0x00001455;
    y = mul_q16(y, xx);
    y += 0xFFFF5AA7;
    y = mul_q16(y, xx);
    y += 0x0001921F;
    y = mul_q16(y, x);
    if (sign ^ per)
        y = -y;
    return y;
}

inline int cos7q16(int x) {
    return sin7q16(x+ Q16_PI_2);
}

