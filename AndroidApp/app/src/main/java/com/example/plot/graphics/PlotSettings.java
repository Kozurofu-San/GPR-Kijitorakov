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
import java.util.Objects;

public class PlotSettings extends PreferenceFragmentCompat{
    private static Plot plot;
    public static void setPlot(Plot p){
        plot = p;
    }
    private static String id;

    ArrayList<Paint> paintList = plot.d.paintList;
    Paint paint;
    ArrayList<String> legendList = plot.d.legendList;
    String legend;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences_plot,rootKey);

        PreferenceScreen s = findPreference("parentScreen");
        assert s!= null;
        Preference p, pp;
        SwitchPreference sp;
        Drawable d;

        id = plot.id;

        p = findPreference("prefGrid");
        assert p!= null;
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                GridSettings.setGraphic(plot);

                return false;
            }
        });
        d = p.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, plot.colorText);
        p.setIcon(d);

        p = findPreference("prefLine");
        assert p != null;
        for (int i=0; i<paintList.size(); i++) {
            paint = paintList.get(i);
            legend = legendList.get(i);

            pp = new Preference(requireContext());
            pp.setTitle(p.getTitle()+Integer.toString(i));
            pp.setSummary(legend);
            d = p.getIcon();
            assert d != null;
            DrawableCompat.setTint(d, paint.getColor());
            pp.setIcon(R.drawable.ic_chart);
            pp.setFragment(p.getFragment());
            int index = i;
            pp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    PlotLineSettings.setPlot(plot, index);
                    return false;
                }
            });
            s.addPreference(pp);
        }
        s.removePreference(p);

        sp = findPreference("switchAntiAliasing");
        assert sp!=null;
        sp.setKey(sp.getKey()+id);
        sp.setChecked(plot.a.switchAntiAliasing);
        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                plot.a.switchAntiAliasing = (Boolean)newValue;
                for (int i=0; i<paintList.size(); i++)
                    paintList.get(i).setAntiAlias(plot.a.switchAntiAliasing);
                return true;
            }
        });
        d = sp.getIcon();
        assert d != null;
        DrawableCompat.setTint(d, plot.colorText);
        sp.setIcon(d);
    }

    public static void restoreSettings(Context context, Plot p) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        p.a.switchAntiAliasing = sp.getBoolean("switchAntiAliasing" + p.id, true);

//        PlotLineSettings.restoreSettings(context,plot);
    }
}
