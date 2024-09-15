package com.example.plot.dsp;

import android.util.Log;

public class Waveform {

    @SuppressWarnings("unused")
    public static void chirpLinear(float[] t, float f0, float t1, float f1, float phi, float[] out){
        if (t.length != out.length)
            Log.e("chirp","Length mismatch");
        float beta = (f1-f0)/t1;
        for (int i=0; i<out.length; i++)
            out[i] = (float) (Math.cos(2*Math.PI*(f0+0.5*beta*t[i])*t[i]+Numeric.deg2rad(phi)));
    }

    @SuppressWarnings("unused")
    public static void chirpQuad(float[] t, float f0, float t1, float f1, float phi, float[] out){
        if (t.length != out.length)
            Log.e("chirp","Length mismatch");
        float beta = (f1-f0)/(t1*t1);
        for (int i=0; i<out.length; i++)
            out[i] = (float) (Math.cos(2*Math.PI*(f0+0.3333333333333*beta*t[i]*t[i])*t[i]+Numeric.deg2rad(phi)));
    }

    @SuppressWarnings("unused")
    public static void square(){

    }

    @SuppressWarnings("unused")
    public static float sinc(float x){
        if (x == 0)
            return 1;
        else
            return (float)(Math.sin(x)/x);
    }

    @SuppressWarnings("unused")
    public static void sinc(float[] in){
        for (int i=0; i<in.length; i++)
            in[i] = sinc(in[i]);
    }

    @SuppressWarnings("unused")
    public static void sinc(float[] in, float[] out){
        if (in.length != out.length)
            Log.e("sinc","Length mismatch");
        for (int i=0; i<out.length; i++)
            out[i] = sinc(in[i]);
    }
}
