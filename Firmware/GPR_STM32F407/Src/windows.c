#include "windows.h"

float zerothOrderBessel(float x) {
    float eps = 0.000001f;
    float besselValue = 0;
    float term = 1;
    float m = 0;
    while (term > eps * besselValue) {
        besselValue += term;
        ++m;
        term *= (x * x) / (4 * m * m);
    }
    return besselValue;
}

void window_compute(float win[], int N, int index) {
    int i;
    float alpha;

    switch (index) {
    case WINDOW_COS:
        for (i = 0; i < N; i++)
            win[i] = (float)(0.5 - 0.5 * cosf(2 * M_PI * i / (N - 1)));
        break;
    case WINDOW_BLACKMAN_HARRIS:
        for (i = 0; i < N; i++)
            win[i] = (float)(0.35875f
                - 0.48829f * cosf(2 * M_PI * i / (N - 1))
                + 0.14128f * cosf(4 * M_PI * i / (N - 1))
                - 0.01168f * cosf(6 * M_PI * i / (N - 1)));
        break;
    case WINDOW_TUKEY:
        alpha = 0.25f;
        for (i = 0; i < N; i++) {
            if (i < alpha * N)
                win[i] = (float)(2 * alpha * (1 + cosf(2 * M_PI / (2 * alpha) * ((float)i / ((float)(N - 1)) - alpha))));
            if ((i >= alpha * N) & (i < (1 - alpha) * N))
                win[i] = 1;
            if (i >= (1 - alpha) * N)
                win[i] = (float)(2 * alpha * (1 + cosf(2 * M_PI / (2 * alpha) * ((float)i / ((float)(N - 1)) - alpha - 1))));
        }
        break;
    case WINDOW_HAMMING:
        for (i = 0; i < N; i++)
            win[i] = (float)(0.54 - 0.46 * cosf(2 * M_PI * i / N));
        break;
    case WINDOW_HANNING:
        for (i = 0; i < N; i++)
            win[i] = (float)(0.5 - 0.5 * cosf(2 * M_PI * i / N));
        break;
    case WINDOW_FLATTOP:
        for (i = 0; i < N; i++)
            win[i] = (float)(0.21557895f
                - 0.41663158f * cosf(2 * M_PI * i / (N - 1))
                + 0.277263158f * cosf(4 * M_PI * i / (N - 1))
                - 0.083578947f * cosf(6 * M_PI * i / (N - 1))
                + 0.006947368f * cosf(8 * M_PI * i / (N - 1)));
        break;
    case WINDOW_KAISER:
        alpha = 4;
        float K;
        for (i = 0; i < N; ++i) {
            K = 2.f * i / N - 1.f;
            win[i] = zerothOrderBessel((float)(alpha * sqrtf(1.f - K * K))) /
                zerothOrderBessel(alpha);
        }

        break;
    case WINDOW_GAUSS:
        alpha = 3.5f;
        float stdev = (N - 1) / (2 * alpha);
        for (i = 0; i < N; i++)
            win[i] = (float)expf(-0.5f * powf(((i - (N - 1) / 2.f) / stdev), 2));
        break;
    case WINDOW_BARTLETT_HANN:
        for (i = 0; i < N; i++)
            win[i] = (float)(0.62f
                - 0.48f * (fabsf((float)i / N - 0.5f))
                + 0.38f * cosf(2 * M_PI * ((float)i / N - 0.5f)));
        break;
    case WINDOW_TRIANGULAR:
        if (N % 2 == 0) {     // Even
            for (i = 0; i < N; i++) {
                if ((i >= 0) & (i < N / 2))
                    win[i] = (float)(2 * i) / (N - 1);
                else
                    win[i] = 2.f - (float)(2 * i) / (N - 1);
            }
        }
        else {   // Odd
            for (i = 0; i < N; i++) {
                if (i >= 0 & i <= N / 2)
                    win[i] = (float)(2 * (i + 1)) / N;
                else
                    win[i] = 2.f - (float)(2 * (i + 1)) / N;
            }
        }
        break;
    case WINDOW_HANN_POISSON:
        alpha = 1.5f;
        float N2 = (float)(N - 1) / 2;
        for (i = 0; i < N; i++)
            win[i] = (float)(0.5 * (1.0 + cosf(M_PI * (i - N2) / N2))
                * expf(-alpha * fabsf(i - N2) / N2));
        break;
    default:
    case WINDOW_RECT:
        for (i = 0; i < N; i++)
            win[i] = 1;
        break;
    }
}
