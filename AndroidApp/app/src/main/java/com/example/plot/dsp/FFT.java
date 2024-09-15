package com.example.plot.dsp;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class FFT {
    float evenRe, evenIm, oddRe, oddIm;
    private final int N;
    private final float[] wRe, wIm;
    private final float[] tempRe,tempIm;
    private final float[] tempRe0, tempIm0;
    private final int[] iR;

    public FFT(int N){
        this.N = N;
        wRe = new float[N]; wIm = new float[N];
        tempRe = new float[N]; tempIm = new float[N];
        tempRe0 = new float[N]; tempIm0 = new float[N];
        iR = new int[N];
        int logN = -1;
        while (N > 0)
        {
            logN++;
            N /= 2;
        }
        for (int i = 0; i < this.N; i++) {
            // Twiddle factors
            if (i < this.N / 2)
            {
                wRe[i] = (float)Math.cos(2 * Math.PI * i / this.N);
                wIm[i] = -(float)Math.sin(2 * Math.PI * i / this.N);
            }
            // Shuffle indices
            int r = 0, p = i;
            for (int l=0; l<logN; l++) {
                r <<= 1;
                r |= p & 1;
                p >>= 1;
            }
            iR[i] = r;
        }
    }

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private int i, j, n, step, x, k, cnt, i0, i1;
    public void transform(@NonNull float[] inRe, float[] inIm,
                          float[] outRe,float[] outIm){
        Arrays.fill(outRe, 0);
        Arrays.fill(outIm, 0);
        n = inRe.length;
        for (i = 0; i < N; i++) {
            j = iR[i];
            if (j<n) {
                outRe[i] = inRe[j];
                outIm[i] = inIm[j];
            }
        }
        n = 2;
        step = N / 2;
        while (n<=N)
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
//        for (i=0; i<outIm.length; i++)
//            outIm[i] = -outIm[i];
    }

    @SuppressWarnings("unused")
    void transformInverse(float[] inRe, float[] inIm, float[] outRe, float[] outIm){
        if (Arrays.equals(inRe, outRe)) {
            System.arraycopy(inRe, 0, tempRe0, 0, inRe.length);
            inRe = tempRe0;
        }
        if (Arrays.equals(inIm, outIm)) {
            System.arraycopy(inIm, 0, tempIm0, 0, inIm.length);
            inIm = tempIm0;
        }
        for (int i=0; i<inIm.length; i++)
            inIm[i] = -inIm[i];
        transform(inRe,inIm,outRe,outIm);
        for (int i=0; i<inIm.length; i++)
            inIm[i] = -inIm[i];
        for (int i=0; i<outIm.length; i++) {
            outRe[i] /= N;
            outIm[i] = -outIm[i] / N;
        }
    }

    @SuppressWarnings("unused")
    public void hilbert(float[] inRe, float[] inIm){
        Arrays.fill(inIm,0);
        transform(inRe,inIm,tempRe,tempIm);
        int i;
        for (i=0; i<tempRe.length; i++) {
            if (i > 0 & i < tempRe.length / 2) {
                tempRe[i] = 2 * tempRe[i];
                tempIm[i] = 2 * tempIm[i];
            } else if (i > tempRe.length / 2){
                tempRe[i] = 0;
                tempIm[i] = 0;
            }
        }
        transformInverse(tempRe,tempIm,tempRe,tempIm);
        for (i=0; i<inIm.length; i++)
            inIm[i] = tempIm[i];
    }

}


