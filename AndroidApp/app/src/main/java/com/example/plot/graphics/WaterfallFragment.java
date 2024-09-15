package com.example.plot.graphics;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.plot.R;

public class WaterfallFragment extends Fragment implements View.OnClickListener {
    DrawThread drawThread;
    private Waterfall waterfall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watefall, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        waterfall = view.findViewById(R.id.waterfall);
        waterfall.id = Integer.toString((char) this.getId());
        GridSettings.restoreSettings(requireContext(),waterfall);
        WaterfallSettings.restoreSettings(requireContext(),waterfall);
        drawThread = DrawThread.createSaved(getViewModelStore(),getLifecycle());
        drawThread.setValues(waterfall);
        ImageView buttonConfig = view.findViewById(R.id.imageButtonConfigWaterfall);
        buttonConfig.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButtonConfigWaterfall:
                SettingsActivity.settingsClass = this.getClass();
                WaterfallSettings.setPlot(waterfall);
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        drawThread.getValues(waterfall);
        if (requireActivity().isFinishing())
            drawThread.threadStop();
    }
}