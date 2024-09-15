package com.example.plot.graphics;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.example.plot.R;

import org.jetbrains.annotations.Contract;

public class WaterfallSettings extends PreferenceFragmentCompat {

    static Waterfall waterfall;
    public static void setPlot(Waterfall w){
        waterfall = w;
    }
    private static String id;

    private static final Bitmap bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
    private static final int[] colors = new int[bitmap.getWidth()*bitmap.getHeight()];
    private static final float[] x = new float[bitmap.getWidth()];
    private static Resources resources;

    private static final Preference.OnPreferenceChangeListener
    listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            String key = preference.getKey();
            key = key.substring(0,key.length()-id.length());
            if (preference instanceof ListPreference){
                String stringValue = newValue.toString();
                ListPreference lp = (ListPreference) preference;
                switch (key){
                    case "nColormap":
                        if (waterfall.s.nColormap!=lp.findIndexOfValue(stringValue))
                            waterfall.s.nColormap = (char) lp.findIndexOfValue(stringValue);
                        lp.setIcon(WaterfallSettings.drawIcon(waterfall.s.nColormap));
                        break;
                }

                lp.setValueIndex(lp.findIndexOfValue(stringValue));
                preference.setSummary(stringValue);
            }
            else if (preference instanceof CheckBoxPreference){

            }
            else if (preference instanceof SwitchPreference){
                switch (key) {
                    case "switchAutoscalingWaterfall":
                        waterfall.s.switchAutoscalingWaterfall = (Boolean)newValue;
                        break;
                    case "switchHardwareInterpolation":
                        waterfall.s.switchHardwareInterpolation = (Boolean)newValue;
                        waterfall.d.paint.setFilterBitmap(waterfall.s.switchHardwareInterpolation);
                        break;
                }
            }
            else if (preference instanceof EditTextPreference){
                preference.setSummary(newValue.toString());
            }
            return true;
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_waterfall,rootKey);

        Preference p;
        SwitchPreference sp;
        ListPreference lp;
        Drawable d;

        p = findPreference("prefGrid");
        assert p!= null;
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                GridSettings.setGraphic(waterfall);
                return false;
            }
        });
        d = p.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, waterfall.colorText);
        p.setIcon(d);

        resources = getResources();
        for (int i=0; i<x.length; i++)
            x[i] = (float)i/(x.length-1);

        id = waterfall.id;
        lp = findPreference("nColormap");
        assert lp != null;
        lp.setKey(lp.getKey()+id);
        String[] str = getResources().getStringArray(R.array.colormap_variants);
        lp.setDialogIcon(R.drawable.ic_color);
        lp.setIcon(drawIcon(waterfall.s.nColormap));
        lp.setSummary(str[waterfall.s.nColormap]);
        lp.setValueIndex(waterfall.s.nColormap);
        lp.setOnPreferenceChangeListener(listener);

        sp = findPreference("switchAutoscalingWaterfall");
        assert sp != null;
        sp.setKey(sp.getKey()+id);
        sp.setChecked(waterfall.s.switchAutoscalingWaterfall);
        sp.setOnPreferenceChangeListener(listener);
        d = sp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, waterfall.colorText);
        sp.setIcon(d);

        sp = findPreference("switchHardwareInterpolation");
        assert sp != null;
        sp.setKey(sp.getKey()+id);
        sp.setChecked(waterfall.s.switchHardwareInterpolation);
        sp.setOnPreferenceChangeListener(listener);
        d = sp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, waterfall.colorText);
        sp.setIcon(d);


    }

    @NonNull
    @Contract("_ -> new")
    public static Drawable drawIcon(char nColormap){
        Colormap.arrayToInt(x,colors,nColormap);
        for (int j=1; j<bitmap.getHeight(); j++)
            for (int i=0; i<bitmap.getWidth(); i++)
                colors[j*bitmap.getWidth()+i] = colors[i];
        bitmap.setPixels(colors,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        return new BitmapDrawable(resources,bitmap);
    }

    public static void restoreSettings(Context context,Waterfall w){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String[] s = context.getResources().getStringArray(R.array.colormap_variants);
        String s0 = sp.getString("nColormap"+w.id,s[2]);
        w.s.nColormap = 0;
        for (char i=0; i<s.length; i++)
            if (s[i].equals(s0)) {
                w.s.nColormap = i;
                break;
            }
        w.s.switchAutoscalingWaterfall
                = sp.getBoolean("switchAutoscalingWaterfall"+w.id,false);
        w.s.switchHardwareInterpolation
                = sp.getBoolean("switchHardwareInterpolation",false);
        w.d.paint.setFilterBitmap(w.s.switchHardwareInterpolation);
    }
}
