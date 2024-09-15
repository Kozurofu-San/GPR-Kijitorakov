package com.example.plot.dsp;

import androidx.annotation.NonNull;

public class Windows {
    static public final int
            RECT = 0,
            BLACKMAN_HARRIS = 1,
            KAISER = 2,
            FLATTOP = 3,
            COS = 4,
            TUKEY = 5,
            HAMMING = 6,
            HANNING = 7,
            GAUSS = 8,
            BARTLETT_HANN = 9,
            TRIANGULAR = 10,
            HANN_POISSON = 11;

    static public final char
            WINDOW_FULL = 0,
            WINDOW_LEFT_HALF = 1,
            WINDOW_RIGHT_HALF = 2;

    public static void compute(@NonNull float[] win, int index) {
        int N = win.length, i;
        float alpha;
        switch (index) {
            case COS:
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.5 - 0.5*Math.cos(2 * Math.PI * i / (N-1)));
                break;
            case BLACKMAN_HARRIS:
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.35875f
                            - 0.48829f * Math.cos(2 * Math.PI * i / (N - 1))
                            + 0.14128f * Math.cos(4 * Math.PI * i / (N - 1))
                            - 0.01168f * Math.cos(6 * Math.PI * i / (N - 1)));
                break;
            case TUKEY:
                float r = 0.25f;
                for (i = 0; i < N; i++) {
                    if (i < r * N)
                        win[i] = (float) (2 * r * (1 + Math.cos(2 * Math.PI / (2 * r) * ((float) i / ((float) (N - 1)) - r))));
                    if (i >= r * N & i < (1 - r) * N)
                        win[i] = 1;
                    if (i >= (1 - r) * N)
                        win[i] = (float) (2 * r * (1 + Math.cos(2 * Math.PI / (2 * r) * ((float) i / ((float) (N - 1)) - r - 1))));
                }
                break;
            case HAMMING:
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.54 - 0.46 * Math.cos(2 * Math.PI * i / N));
                break;
            case HANNING:
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * i / N));
                break;
            case FLATTOP:
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.21557895f
                            - 0.41663158f * Math.cos(2 * Math.PI * i / (N - 1))
                            + 0.277263158f * Math.cos(4 * Math.PI * i / (N - 1))
                            - 0.083578947f * Math.cos(6 * Math.PI * i / (N - 1))
                            + 0.006947368f * Math.cos(8 * Math.PI * i / (N - 1)));
                break;
            case KAISER:
                alpha = 4;
                float K;
                for (i = 0; i < N; ++i) {
                    K = 2f * i / N - 1f;
                    win[i] = zerothOrderBessel((float) (alpha * Math.sqrt(1f - K * K))) /
                            zerothOrderBessel(alpha);
                }

                break;
            case GAUSS:
                alpha = 3.5f;
                float stdev = (N - 1) / (2 * alpha);
                for (i = 0; i < N; i++)
                    win[i] = (float) Math.exp(-0.5f * Math.pow(((i - (N - 1) / 2f) / stdev), 2));
                break;
            case BARTLETT_HANN:
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.62f
                            - 0.48f * (Math.abs((float) i / N - 0.5f))
                            + 0.38f * Math.cos(2 * Math.PI * ((float) i / N - 0.5f)));
                break;
            case TRIANGULAR:
                if (N % 2 == 0) {     // Even
                    for (i = 0; i < N; i++) {
                        if (i >= 0 & i < N / 2)
                            win[i] = (float) (2 * i) / (N - 1);
                        else
                            win[i] = 2f - (float) (2 * i) / (N - 1);
                    }
                } else {   // Odd
                    for (i = 0; i < N; i++) {
                        if (i >= 0 & i <= N / 2)
                            win[i] = (float) (2 * (i + 1)) / N;
                        else
                            win[i] = 2f - (float) (2 * (i + 1)) / N;
                    }
                }
                break;
            case HANN_POISSON:
                alpha = 1.5f;
                float N2 = (float) (N - 1) / 2;
                for (i = 0; i < N; i++)
                    win[i] = (float) (0.5 *(1.0+Math.cos(Math.PI*(i-N2)/N2))
                         *Math.exp(-alpha*Math.abs(i-N2)/N2));
                break;
            default:
            case RECT:
                for (i = 0; i < N; i++)
                    win[i] = 1;
                break;
        }
    }

    private static float zerothOrderBessel(float x) {
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
}
