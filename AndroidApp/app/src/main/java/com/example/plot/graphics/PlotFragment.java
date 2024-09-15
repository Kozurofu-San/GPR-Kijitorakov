package com.example.plot.graphics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.plot.R;
import com.example.plot.dsp.Numeric;

import java.util.ArrayList;
import java.util.Objects;

public class PlotFragment extends Fragment implements View.OnClickListener {
    Plot plot;
    DrawThread drawThread;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        plot = view.findViewById(R.id.plot);
        plot.id = Integer.toString((char) this.getId());
        GridSettings.restoreSettings(requireContext(),plot);
        PlotSettings.restoreSettings(requireContext(),plot);
        ImageView buttonConfig = view.findViewById(R.id.imageButtonConfigPlot);
        buttonConfig.setOnClickListener(this);
        drawThread = DrawThread.createSaved(getViewModelStore(),getLifecycle());
        drawThread.setValues(plot);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()){
            case R.id.imageButtonConfigPlot:
                SettingsActivity.settingsClass = this.getClass();
                PlotSettings.setPlot(plot);
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        drawThread.getValues(plot);
        if (requireActivity().isFinishing())
            drawThread.threadStop();
    }
}
