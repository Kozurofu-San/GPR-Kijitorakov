package com.example.plot.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class DrawThread extends ViewModel implements DefaultLifecycleObserver {
    Graphic g;
    Thread thread;
    boolean runThread = true;
    Semaphore sem,semTimer,isDrawn;
    protected final Semaphore readyData = new Semaphore(1);
    public static final ArrayList<Semaphore> semaphoresToAcquire = new ArrayList<>();
    public static final ArrayList<Semaphore> semaphoresToRelease = new ArrayList<>();
    Canvas canvas;
    int cnt = 0;
    boolean isPaused = false;
    CountDownTimer countDownTimer;
    boolean threadCreated = false;


    DrawThread(){
//        while (!g.surfaceCreated);
        semTimer = new Semaphore(1);
        sem = new Semaphore(0,true);
        isDrawn = new Semaphore(0);
        semaphoresToAcquire.add(0, isDrawn);
        semaphoresToRelease.add(0, sem);
//        long ms = (long) (1000 / g.view.fps);
        long ms = 16;
        countDownTimer = new CountDownTimer(ms, ms) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                semTimer.release();
            }
        };
        thread = new Thread(() -> {
//            g.surfaceCreated = true;
//            while (!g.surfaceCreated);
            while (runThread) {
                try {
                    semTimer.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isPaused)
                    countDownTimer.start();
                try {
                    sem.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                canvas = null;
                if (g.isInit) {
                    try {
                        isDrawn.drainPermits();
                        canvas = g.holder.lockCanvas(null);
                        synchronized (g.holder) {
                            g.draw(canvas);
                        }
                    } finally {
                        if (canvas != null) {
                            g.holder.unlockCanvasAndPost(canvas);
                            if (g.dataUpdated == Graphic.UPDATED)
                                isDrawn.release();
                        }
                    }
                }
                else
                    g.dataUpdated = Graphic.UPDATED;
            }
        });
        thread.setName("Draw "+thread.getName());
        thread.setPriority(Thread.NORM_PRIORITY+1);
        threadCreated = true;
    }

    public static void drawnow(boolean wait){
        for (int i = 0; i<semaphoresToRelease.size(); i++)
            semaphoresToRelease.get(i).release();
        if (wait)
            for (int i=0; i<semaphoresToAcquire.size(); i++){
                try {
                    semaphoresToAcquire.get(i).acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    void setValues(Graphic p) {
        p.semaphore = sem;
        p.setData(g);
        if (p instanceof Plot)
            ((Plot) p).setData((Plot) g);
        else if (p instanceof Waterfall)
            ((Waterfall) p).setData((Waterfall) g);
        if (thread.getState()== Thread.State.NEW)
            thread.start();
        p.threadCreated = threadCreated;
        g = p;
//        semTimer.release();
    }

    void getValues(Graphic p){
//        semTimer.drainPermits();
    }

    void threadStop(){
        runThread = false;
        semTimer.release();
        sem.release();
        isPaused = false;
        countDownTimer.start();
        boolean retry = true;
        while (retry){
            try {
                thread.join();
                retry = false;
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    static class Factory implements ViewModelProvider.Factory {
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new DrawThread();
        }
    }
    static DrawThread createSaved(ViewModelStore activity, Lifecycle lifecycle){
        DrawThread saved = new ViewModelProvider(activity,new Factory())
                .get("Fragment", DrawThread.class);
        lifecycle.addObserver(saved);
        return saved;
    }

}
