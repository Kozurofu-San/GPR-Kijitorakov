package com.example.plot.graphics;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;

import com.example.plot.MainActivity;
import com.example.plot.R;
import com.example.plot.SettingsFragment;
import com.example.plot.graphics.PlotFragment;
import com.example.plot.graphics.PlotSettings;
import com.example.plot.graphics.WaterfallFragment;
import com.example.plot.graphics.WaterfallSettings;

public class SettingsActivity extends AppCompatActivity {

    AlertDialog.Builder dialog;

    public static Class<?> settingsClass;
    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        PreferenceFragmentCompat settingsFragment = null;

        if (settingsClass == MainActivity.class) {
            setTitle("Settings");
            settingsFragment = new SettingsFragment();
        }
        else if (settingsClass == PlotFragment.class){
            setTitle("Plot Settings");
            settingsFragment = new PlotSettings();
        }
        else if (settingsClass == WaterfallFragment.class){
            setTitle("Waterfall Settings");
            settingsFragment = new WaterfallSettings();
        }
        if (settingsFragment != null)
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, settingsFragment)
                .commit();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (settingsClass==MainActivity.class)
            getMenuInflater().inflate(R.menu.menu_settings,menu);
        else if (settingsClass==PlotFragment.class)
            getMenuInflater().inflate(R.menu.menu_settings,menu);
        else if (settingsClass==WaterfallFragment.class)
            getMenuInflater().inflate(R.menu.menu_settings,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (settingsClass==PlotFragment.class) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            switch (item.getItemId()) {
                case (int)R.id.menu_info:
                    builder.setTitle("Information")
                            .setMessage("Graphics, DSP and Numerical methods\n\n" +
                                    "Created by RF R&D engineer Kozurofu-San\n" +
                                    "2022\n" +
                                    "All rights are not reserved")
                            .setIcon(R.drawable.ic_info)
                            .setPositiveButton("Roger that", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).create().show();

                    break;
                case (int)R.id.menu_help:
                    builder.setTitle("Help")
                            .setMessage("This is menu")
                            .setIcon(R.drawable.ic_help)
                            .setPositiveButton("Gotcha", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).create().show();
                    break;
                default:
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
