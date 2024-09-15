package com.example.plot.graphics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.example.plot.R;

import java.util.ArrayList;

public class PlotLineSettings extends PreferenceFragmentCompat {
    private static Plot plot;
    private static int index;
    private static String legend;
    private static Paint paint;

    public static void setPlot(Plot p,int i){
        plot = p;
        index = i;
    }

    private static String id;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        id = plot.id + index;
        ArrayList<Paint> paintList = plot.d.paintList;
        paint = paintList.get(index);
        ArrayList<String> legendList = plot.d.legendList;
        legend = legendList.get(index);

        PreferenceScreen screen;
        PreferenceCategory category;
        CheckBoxPreference checkBox;
        SwitchPreference switchPref;
        EditTextPreference editText;
        Drawable icon;

        screen = getPreferenceManager().createPreferenceScreen(requireContext());

        category = new PreferenceCategory(requireContext());
        category.setTitle("Line settings: " + legend);
        screen.addPreference(category);

//        category = (PreferenceCategory) findPreference("Line settings");
//        assert category != null;
//        category.setTitle(category.getTitle()+legend);

        editText = new EditTextPreference(requireContext());
        category.addPreference(editText);
        editText.setKey("editLineLegend" + id);
        editText.setTitle("Name");
        editText.setSummary(legend);
        editText.setText(legend);
        editText.setIcon(R.drawable.ic_line_width);
        editText.setDialogTitle("Legend");
        editText.setDialogMessage("Enter the line name");
        editText.setDialogIcon(R.drawable.ic_name);
        editText.setOnPreferenceChangeListener(listener);

        checkBox = new CheckBoxPreference(requireContext());
        category.addPreference(checkBox);
        checkBox.setKey("checkLineEnable" + id);
        checkBox.setTitle("Line enable");
        checkBox.setSummary("Show line");
        checkBox.setChecked(paint.getAlpha() != 0);
        checkBox.setDefaultValue(paint.getAlpha() != 0);
        icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_chart);
        assert icon != null;
        DrawableCompat.setTint(icon, Color.WHITE);
        checkBox.setIcon(R.drawable.ic_chart);
        checkBox.setOnPreferenceChangeListener(listener);

        switchPref = new SwitchPreference(requireContext());
        category.addPreference(switchPref);
        switchPref.setKey("switchLineDash" + id);
        switchPref.setTitle("Dash Line");
        switchPref.setSummary("On - dash, off - solid");
        switchPref.setChecked(paint.getPathEffect() != null);
        switchPref.setIcon(R.drawable.ic_dash);
        switchPref.setOnPreferenceChangeListener(listener);

        editText = new EditTextPreference(requireContext());
        category.addPreference(editText);
        editText.setKey("editLineWidth" + id);
        editText.setTitle("Axis Line Width");
        editText.setText(Float.toString(paint.getStrokeWidth()));
        editText.setSummary(Float.toString(paint.getStrokeWidth()));
        editText.setIcon(R.drawable.ic_line_width);
        editText.setOnPreferenceChangeListener(listener);
        editText.setOnBindEditTextListener(digital);

        editText = new EditTextPreference(requireContext());
        category.addPreference(editText);
        editText.setKey("editLineColor" + id);
        editText.setTitle("Line color");
        editText.setSummary(Integer.toHexString(paint.getColor()));
        editText.setText(Integer.toHexString(paint.getColor()));
        editText.setIcon(R.drawable.ic_color);
        icon = editText.getIcon();
        assert icon != null;
        DrawableCompat.setTint(icon, paint.getColor());
        editText.setIcon(icon);
        editText.setOnPreferenceChangeListener(listener);
        editText.setOnBindEditTextListener(digital);

        setPreferenceScreen(screen);
    }


    private static final Preference.OnPreferenceChangeListener
            listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            String key = preference.getKey();
            key = key.substring(0,key.length()-id.length());
            if (preference instanceof ListPreference){
            }
            else if (preference instanceof CheckBoxPreference){
                switch (key) {
                    case "checkLineEnable":
                        if ((boolean)newValue)
                            paint.setAlpha(0xFF);
                        else
                            paint.setAlpha(0x00);
                        break;
                }
            }
            else if (preference instanceof SwitchPreference){
                switch (key) {
                    case "switchLineDash":
                        if ((Boolean)newValue)
                            paint.setPathEffect(plot.dash);
                        else
                            paint.setPathEffect(null);
                        break;
                }
            }
            else if (preference instanceof EditTextPreference){
                switch (key){
                    case "editLineColor":
                        paint.setColor((int)Long.parseLong(newValue.toString(),16));
                        Drawable icon = preference.getIcon();
                        assert icon != null;
                        icon.setTint(paint.getColor());
                        preference.setIcon(icon);
                        break;
                    case "editLineLegend":
                        legend = newValue.toString();
                        plot.d.legendList.set(index,legend);
                        break;
                    case "editLineWidth":
                        paint.setStrokeWidth(Float.parseFloat(newValue.toString()));
                        break;
                }
                preference.setSummary(newValue.toString());
                ((EditTextPreference) preference).setText(newValue.toString());
            }
            return true;
        }
    };


    private static final EditTextPreference.OnBindEditTextListener digital = new EditTextPreference.OnBindEditTextListener() {
        @Override
        public void onBindEditText(@NonNull EditText editText) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    };


}
