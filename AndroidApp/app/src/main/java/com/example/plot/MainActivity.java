package com.example.plot;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.os.Bundle;
import android.support.v4.os.IResultReceiver;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.example.plot.dsp.Complex;
import com.example.plot.dsp.FFT;
import com.example.plot.dsp.Numeric;
import com.example.plot.dsp.Windows;
import com.example.plot.graphics.DrawThread;
import com.example.plot.graphics.Graphic;
import com.example.plot.graphics.GridSettings;
import com.example.plot.graphics.Plot;
import com.example.plot.graphics.SettingsActivity;
import com.example.plot.graphics.Waterfall;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {
    Plot plotSignal, plotSpecter;
    Waterfall waterfall;
    MainThread saved;

    ImageButton imageButtonPlay;
    boolean isRunning = true;
    Semaphore semRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        imageButtonPlay = findViewById(R.id.imageButtonPlay);
        plotSignal = Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.plot2)).requireView().findViewById(R.id.plot);
        plotSpecter = Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.plot1)).requireView().findViewById(R.id.plot);
        waterfall = Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.waterfall1)).requireView().findViewById(R.id.waterfall);

        saved = new ViewModelProvider(getViewModelStore(),new Factory(getApplication()))
                .get("Main", MainThread.class);
        getLifecycle().addObserver(saved);
        saved.setValues(this);
    }

    public void toSettingsActivity(View view){
        SettingsFragment.setClass(saved);
        SettingsActivity.settingsClass = this.getClass();
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    public void playPause(View view){
        if (isRunning) {
            isRunning = false;
            semRunning.drainPermits();
            imageButtonPlay.setImageResource(R.drawable.ic_play);
        }
        else {
            isRunning = true;
            semRunning.release();
            imageButtonPlay.setImageResource(R.drawable.ic_pause);
        }
    }

    private long timeStart;
//    private long time = 0;
    public void tic(){
        timeStart = System.nanoTime();
    }
    public long toc(){
        return System.nanoTime()-timeStart;
    }
}






