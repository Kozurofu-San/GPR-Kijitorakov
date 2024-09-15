#pragma once

#include <math.h>
#include "mathematics.h"
enum {
    WINDOW_RECT = 0,
    WINDOW_BLACKMAN_HARRIS = 1,
    WINDOW_KAISER = 2,
    WINDOW_FLATTOP = 3,
    WINDOW_COS = 4,
    WINDOW_TUKEY = 5,
    WINDOW_HAMMING = 6,
    WINDOW_HANNING = 7,
    WINDOW_GAUSS = 8,
    WINDOW_BARTLETT_HANN = 9,
    WINDOW_TRIANGULAR = 10,
    WINDOW_HANN_POISSON = 11,
};

void window_compute(float win[], int len, int index);
