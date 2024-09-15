package com.example.plot.dsp;

import androidx.annotation.NonNull;

public class Complex {

    // z=x+j*y
    @SuppressWarnings("unused")
    public static void abs(float[] inRe, float[] inIm, float[] out) {
        for (int i = 0; i < inRe.length; i++)
            out[i] = (float) Math.sqrt(inRe[i] * inRe[i] + inIm[i] * inIm[i]);
    }
    @SuppressWarnings("unused")
    public static float abs(float inRe, float inIm) {
            return  (float) Math.sqrt(inRe * inRe + inIm * inIm);
    }

    @SuppressWarnings("unused")
    public static void angle(float[] inRe, float[] inIm, float[] out) {
        for (int i = 0; i < inRe.length; i++)
            out[i] = (float) Math.atan2(inIm[i], inRe[i]);
    }
    @SuppressWarnings("unused")
    public static float angle(float inRe, float inIm) {
        return  (float) Math.atan2(inIm, inRe);
    }

    // z=r*(cos(phi)+j*sin(phi))

    // z=r*exp(j*phi)

    public static void mul(float[] in1Re,float[] in1Im, float[] in2Re, float[] in2Im){
        for (int i=0; i<in1Re.length; i++){
            in1Re[i] = in1Re[i]*in2Re[i]-in1Im[i]*in2Im[i];
            in1Im[i] = in1Re[i]*in2Im[i]+in1Im[i]*in2Re[i];
        }
    }

}
