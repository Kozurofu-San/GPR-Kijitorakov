package com.example.plot;

import android.app.Application;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.plot.dsp.Complex;
import com.example.plot.dsp.FFT;
import com.example.plot.dsp.Numeric;
import com.example.plot.dsp.Windows;
import com.example.plot.graphics.DrawThread;
import com.example.plot.graphics.Plot;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;

class MainThread extends AndroidViewModel implements DefaultLifecycleObserver {
    private static final byte
            CMD_FREQUENCY = 3,
            CMD_SWEEP_PARAMETERS = 6,
            CMD_SWEEP = 9;
    private final Thread thread;
    private boolean running = true;
    private MainActivity a;
    private final Semaphore semaphore;
    private Communication communication;

    byte[] buffer = new byte[10000];

    short fmin = 800,
        fmax = 3000,
        fstep = 20,
        texp = 60,
        power = 3,
        phase = 0;

    static final char
            ANTENNA_DIPOLE = 0,
            ANTENNA_LPDA = 1,
            ANTENNA_SPIRAL = 2;
    char nAntennaTx, nAntennaRx;

    float antTxLpdaMin = 0.01f,
            antTxLpdaMax = 0.15f,
            antTxSpiral = 0.2f,
            antRxLpdaMin = 0.01f,
            antRxLpdaMax = 0.15f,
            antRxSpiral = 0.2f;

    int n, ni, ifmin;
    float[] signalRe, signalIm,
            signalMag, signalPha,
            f, window,
            lpdaLen, lpdaPha;
    float[] siRe, siIm;

    int N;
    FFT fft;
    float[] specterRe, specterIm,
            t, r;
    float dt;

    MainThread(Application application){
        super(application);
        SettingsFragment.setClass(this);
        SettingsFragment.restoreSettings(application.getApplicationContext());

        semaphore = new Semaphore(0,true);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    float frameFps;

                    double v;
                    initArrays();

                    Paint paint = new Paint();
                    paint.setAntiAlias(false);
                    paint.setColor(Color.CYAN);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(4);
                    a.plotSignal.clearChart();
                    Plot.Chart figRe = a.plotSignal.new Chart(f, signalRe, paint, "mag");
                    paint.setColor(Color.MAGENTA);
                    Plot.Chart figIm = a.plotSignal.new Chart(f, signalIm, paint, "pha");
                    a.plotSignal.label("f, Hz", "Specter");
                    a.plotSpecter.clearChart();
                    paint.setColor(Color.GREEN);
                    Plot.Chart figSre = a.plotSpecter.new Chart(r, specterRe, paint, "re");
//                    paint.setColor(Color.YELLOW);
//                    Plot.Chart figSim = a.plotSpecter.new Chart(t, specterIm, paint, "im");
//                    a.plotSpecter.xLim(0,N/2f);
                    a.plotSpecter.label("r, m", "Signal");
                    a.waterfall.initChart(specterRe, r, 500);
                    a.waterfall.setLimits(a.plotSpecter.view);
                    a.waterfall.label("r, m", "N");
                    DrawThread.drawnow(true);

                    try {
                        communication.sem.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setParameters();
                    communication.sem.release();

                    while (running) {

                        while (!a.isRunning) {
                            try {
                                semaphore.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            communication.sem.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (SettingsFragment.isSettingsChanged) {
                            initArrays();
                            setParameters();
                            SettingsFragment.isSettingsChanged = false;
                        }

                        receiveSignal();
//                        figRe.setY(signalMag);
//                        figIm.setY(signalPha);

                        Numeric.mul(signalPha, (float) (Math.PI)/2/2048f);
                        Numeric.sin(signalPha);
                        fft.hilbert(signalPha,signalIm);
//                        Complex.abs(signalPha,signalIm,signalRe);
//                        figSig.setY(signalRe);
                        Numeric.atan2(signalPha,signalIm);
                        Numeric.sub(signalPha,lpdaPha);
                        Numeric.sub(signalPha,lpdaPha);
                        Numeric.sub(signalPha,lpdaPha);
                        Numeric.sub(signalPha,lpdaPha);
                        Numeric.equ(signalPha,signalRe);
                        Numeric.cos(signalRe);
//                        figSig.setY(signalRe);


                        Numeric.mul(signalMag,30f/2048f/20f);
                        Numeric.pow(10,signalMag);
                        Numeric.pow(signalMag,0.125f);
                        Numeric.mul(signalRe,signalMag);
                        Numeric.equ(signalPha,signalIm);
                        Numeric.sin(signalIm);
                        Numeric.mul(signalIm,signalMag);

                        Numeric.mul(signalRe,window);
                        Numeric.mul(signalIm,window);

                        figIm.setY(signalIm);
                        figRe.setY(signalRe);

                        Arrays.fill(siRe,0);
                        Arrays.fill(siIm,0);
                        System.arraycopy(signalRe,0,siRe,ifmin,signalRe.length);
                        System.arraycopy(signalIm,0,siIm,ifmin,signalIm.length);
//                        Numeric.conv(siRe,siRe,siRe);
//                        Numeric.conv(siIm,siIm,siIm);
                        v = Numeric.mean(siRe);
                        Numeric.sub(siRe, (float) v);
                        v = Numeric.mean(siIm);
                        Numeric.sub(siIm, (float) v);
                        fft.transform(siRe, siIm, specterRe, specterIm);
//                        Complex.mul(specterRe, specterIm,specterRe, specterIm);
                        Complex.abs(specterRe, specterIm, specterRe);
                        Numeric.div(specterRe,n);

                        figSre.setY(specterIm);
                        a.waterfall.setChart(specterIm);
                        communication.sem.release(1);
                        DrawThread.drawnow(true);
                    }
                }
            }
        });
        thread.setName("Loop "+thread.getName());
    }

    void initArrays(){
        n = (fmax-fmin)/fstep;
        signalRe = new float[n]; signalIm = new float[n];
                signalMag = new float[n]; signalPha = new float[n];
                f = new float[n]; window = new float[n];
                lpdaLen = new float[n]; lpdaPha = new float[n];
        int i,j;
        ni = fmax/fstep;
        ifmin = fmin/fstep;
        siRe = new float[ni]; siIm = new float[ni];
        Windows.compute(window, SettingsFragment.nWindow);
        Numeric.linspace(fmin,fmax-fstep,f);
        Numeric.mul(f, 1e6f);
        Numeric.linspace(0.15f,0.01f, lpdaLen);
        for (i=0; i<n; i++)
            lpdaPha[i] = (float) (2*Math.PI*lpdaLen[i]*f[i]/3e8f);
        N = SettingsFragment.nFft;
        fft = new FFT(N);
        specterRe = new float[N]; specterIm = new float[N];
                t = new float[N]; r = new float[N];
        dt = 1f/(fstep*1e6f)/N;
        for (i=0; i<N; i++)
            t[i] = i*dt;
        for (i=0; i<N; i++)
            r[i] = 3e8f/2f*t[i];
    }

    void setParameters(){

        communication.setByteCnt(0);
        communication.putByteToByteArray(buffer,CMD_SWEEP_PARAMETERS);
        communication.putByteToByteArray(buffer, (byte) 0);
        communication.write(buffer,2);

        communication.read(buffer,3);
        communication.setByteCnt(0);
        //String s = communication.getStringFromByteArray(buffer, 2);
        if (!(Objects.equals(communication.getStringFromByteArray(buffer, 2), "OK")))
            Log.e("USB","not answered");

        communication.setByteCnt(0);
        communication.putShortToByteArray(buffer,fmin);
        communication.putShortToByteArray(buffer,fmax);
        communication.putShortToByteArray(buffer,fstep);
        communication.putShortToByteArray(buffer,texp);
        communication.putShortToByteArray(buffer,power);
        communication.putShortToByteArray(buffer,phase);
        communication.write(buffer, 12);

        communication.setByteCnt(0);
        communication.read(buffer,3);
        if (!(Objects.equals(communication.getStringFromByteArray(buffer, 2), "OK")))
            Log.e("USB","not answered");

    }

    void receiveSignal(){
        communication.setByteCnt(0);
        communication.putByteToByteArray(buffer,CMD_SWEEP);
        communication.putByteToByteArray(buffer, (byte) 0);
        communication.write(buffer,2);
        communication.read(buffer,(2*n+1)*2);
        communication.setByteCnt(0);
        communication.getEvenShortFromByteArray(buffer,signalMag);
        communication.getOddShortFromByteArray(buffer,signalPha);
    }

    void setValues(MainActivity activity){
        activity.semRunning = semaphore;
        if (this.a!=null)
            activity.isRunning = this.a.isRunning;
        this.a = activity;
        if (communication==null)
            communication = new Communication(a);
        if (thread.getState()== Thread.State.NEW)
            thread.start();
    }



    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);

        if (a.isFinishing()) {
            running = false;
            a.isRunning = true;
            semaphore.release(10);
            boolean retry = true;

            for (int i = 0; i < DrawThread.semaphoresToAcquire.size(); i++)
                DrawThread.semaphoresToAcquire.get(i).release();
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        if (a.isRunning){
            a.imageButtonPlay.setImageResource(R.drawable.ic_pause);
            semaphore.release();
        }
        else
            a.imageButtonPlay.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        if (a.isRunning) {
            a.isRunning = false;
            semaphore.drainPermits();
            while (semaphore.availablePermits()==1);
            a.isRunning = true;
        }

    }
}
class Factory implements ViewModelProvider.Factory {
    Application application;

    Factory(Application application){
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainThread.class))
            return (T) new MainThread(application);
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}