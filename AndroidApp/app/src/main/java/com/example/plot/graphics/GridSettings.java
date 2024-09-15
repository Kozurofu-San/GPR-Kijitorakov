package com.example.plot.graphics;

import android.content.Context;
import android.content.SharedPreferences;
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

public class GridSettings extends PreferenceFragmentCompat {
    private static Graphic graphic;
    public static void setGraphic(Graphic g){
        graphic = g;
    }
    private static String id;

    private static final Preference.OnPreferenceChangeListener
            listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            String key = preference.getKey();
            key = key.substring(0,key.length()-id.length());
            if (preference instanceof ListPreference){
                ListPreference lp = (ListPreference) preference;
                lp.setValueIndex(lp.findIndexOfValue((String) newValue));
                preference.setSummary((String) newValue);
            }
            else if (preference instanceof CheckBoxPreference){
                switch (key) {
                    case "checkGridEnable":
                        graphic.a.checkGridEnable = (Boolean)newValue;
                        break;
                }
            }
            else if (preference instanceof SwitchPreference){
                switch (key) {
                    case "switchGridDash":
                        graphic.a.switchGridDash = (Boolean)newValue;
                        break;
                    case "switchAutoscalingPlot":
                        graphic.a.switchAutoscalingPlot = (Boolean)newValue;
                        break;
                    case "switchAntiAliasing":
                        graphic.a.switchAntiAliasing = (Boolean)newValue;
                        graphic.paintGrid.setAntiAlias(graphic.a.switchAntiAliasing);
                        graphic.paintLabel.setAntiAlias(graphic.a.switchAntiAliasing);
                        graphic.paintAxis.setAntiAlias(graphic.a.switchAntiAliasing);
                        graphic.paintText.setAntiAlias(graphic.a.switchAntiAliasing);
                        break;
                }
            }
            else if (preference instanceof EditTextPreference){
                switch (key){
                    case "editGridColor":
                        graphic.paintGrid.setColor((int)Long.parseLong(newValue.toString(),16));
                        Drawable icon = preference.getIcon();
                        assert icon != null;
                        icon.setTint(graphic.paintGrid.getColor());
                        preference.setIcon(icon);
                        break;
                    case "editGridLineWidth":
                        graphic.paintGrid.setStrokeWidth(Float.parseFloat(newValue.toString()));
                        break;
                    case "editLabelX":
                        graphic.a.xLabel = newValue.toString();
                        break;
                    case "editLabelY":
                        graphic.a.yLabel = newValue.toString();
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

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_grid,rootKey);

        PreferenceScreen s = findPreference("parentScreen");
        assert s!=null;
        CheckBoxPreference cp;
        ListPreference lp;
        SwitchPreference sp;
        EditTextPreference ep;
        Drawable d;

        while (id==null)
            id = graphic.id;
        cp = findPreference("checkGridEnable");
        assert cp != null;
        cp.setKey(cp.getKey()+id);
        cp.setChecked(graphic.a.checkGridEnable);
        cp.setOnPreferenceChangeListener(listener);
        d = cp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        cp.setIcon(d);

        sp = findPreference("switchGridDash");
        assert sp != null;
        sp.setKey(sp.getKey()+id);
        sp.setChecked(graphic.a.switchGridDash);
        sp.setOnPreferenceChangeListener(listener);
        d = sp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        sp.setIcon(d);

        sp = findPreference("switchAntiAliasing");
        assert sp != null;
        sp.setKey(sp.getKey()+id);
        sp.setChecked(graphic.a.switchAntiAliasing);
        sp.setOnPreferenceChangeListener(listener);
        d = sp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        sp.setIcon(d);

        ep = getPreferenceManager().findPreference("editGridLineWidth");
        assert ep != null;
        ep.setKey(ep.getKey()+id);
        ep.setText(Float.toString(graphic.paintGrid.getStrokeWidth()));
        ep.setSummary(Float.toString(graphic.paintGrid.getStrokeWidth()));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        d = ep.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        ep.setIcon(d);

        ep = findPreference("editGridColor");
        assert ep != null;
        ep.setKey(ep.getKey()+id);
        d = ep.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.paintGrid.getColor());
        ep.setText(Integer.toHexString(graphic.paintGrid.getColor()));
        ep.setSummary(Integer.toHexString(graphic.paintGrid.getColor()));
        ep.setIcon(d);
        ep.setDialogIcon(d);
        ep.setOnPreferenceChangeListener(listener);

        ep = findPreference("editLabelX");
        assert ep != null;
        ep.setKey(ep.getKey()+id);
        ep.setText(graphic.a.xLabel);
        ep.setSummary(graphic.a.xLabel);
        ep.setOnPreferenceChangeListener(listener);
        d = ep.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        ep.setIcon(d);

        ep = findPreference("editLabelY");
        assert ep != null;
        ep.setKey(ep.getKey()+id);
        ep.setText(graphic.a.yLabel);
        ep.setSummary(graphic.a.yLabel);
        ep.setOnPreferenceChangeListener(listener);
        d = ep.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        ep.setIcon(d);

        sp = findPreference("switchAutoscalingPlot");
        assert sp != null;
        sp.setKey(sp.getKey()+id);
        sp.setChecked(graphic.a.switchAutoscalingPlot);
        sp.setOnPreferenceChangeListener(listener);
        d = sp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        sp.setIcon(d);
        if (graphic instanceof Waterfall)
            s.removePreference(sp);

        cp = findPreference("checkLogX");
        assert cp != null;
        cp.setKey(cp.getKey()+id);
        cp.setChecked(graphic.a.checkLogX);
        cp.setOnPreferenceChangeListener(listener);
        d = cp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        cp.setIcon(d);

        cp = findPreference("checkLogY");
        assert cp != null;
        cp.setKey(cp.getKey()+id);
        cp.setChecked(graphic.a.checkLogY);
        cp.setOnPreferenceChangeListener(listener);
        d = cp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, graphic.colorText);
        cp.setIcon(d);

    }

    public static void restoreSettings(Context context, Graphic g) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        g.a.checkGridEnable = sp.getBoolean("checkGridEnable" + g.id, true);
        g.a.switchGridDash = sp.getBoolean("switchGridDash" + g.id, true);
        g.a.switchAutoscalingPlot = sp.getBoolean("switchAutoscalingPlot" + g.id, false);
        g.a.switchAntiAliasing = sp.getBoolean("switchAutoscalingPlot" + g.id, false);
        g.paintGrid.setAntiAlias(g.a.switchAntiAliasing);
        g.paintAxis.setAntiAlias(g.a.switchAntiAliasing);
        g.paintLabel.setAntiAlias(g.a.switchAntiAliasing);
        g.paintText.setAntiAlias(g.a.switchAntiAliasing);
        g.a.checkLogX = sp.getBoolean("checkLogX" + g.id, false);
        g.a.checkLogY = sp.getBoolean("checkLogY" + g.id, false);
//        PlotLineSettings.restoreSettings(context,plot);
    }
}
