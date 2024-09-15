package com.example.plot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.example.plot.graphics.Graphic;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    static int nFft;
    static int nWindow;
    static char nWindowHalf;
    static boolean isSettingsChanged = false;
    static MainThread m;
    public static void setClass(MainThread mainThread){
        m = mainThread;
    }

    private static final EditTextPreference.OnBindEditTextListener digital = new EditTextPreference.OnBindEditTextListener() {
        @Override
        public void onBindEditText(@NonNull EditText editText) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    };

    private static final Preference.OnPreferenceChangeListener 
            listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            if (preference instanceof ListPreference){
                String stringValue = newValue.toString();
                ListPreference lp = (ListPreference) preference;
                switch (lp.getKey()){
                    case "nFft":
                        if (nFft!=Integer.parseInt(stringValue))
                            isSettingsChanged = true;
                        nFft = Integer.parseInt(stringValue);
                        break;
                    case "nWindow":
                        if (nWindow!=lp.findIndexOfValue(stringValue))
                            isSettingsChanged = true;
                        nWindow = lp.findIndexOfValue(stringValue);
                        break;
                    case "nWindowHalf":
                        if (nWindowHalf!=lp.findIndexOfValue(stringValue))
                            isSettingsChanged = true;
                        nWindowHalf = (char) lp.findIndexOfValue(stringValue);
                        break;
                }

                lp.setValueIndex(lp.findIndexOfValue(stringValue));
                preference.setSummary(stringValue);
            }
            else if (preference instanceof CheckBoxPreference){
                
            }
            else if (preference instanceof EditTextPreference){
                short temp;
                EditTextPreference ep = (EditTextPreference) preference;
                String stringValue = newValue.toString();
                ep.setSummary(newValue.toString());
            }
            return false;
        }
    };

    private static void bindSummary(@NonNull Preference preference){
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(),""));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences,rootKey);

        ListPreference lp;
        EditTextPreference ep;

        lp = findPreference("nFft");
        assert lp != null;
        lp.setSummary(Integer.toString(nFft));
        lp.setValueIndex(lp.findIndexOfValue(Integer.toString(nFft)));
        bindSummary(lp);

        lp = findPreference("nWindow");
        assert lp != null;
        String[] s = getResources().getStringArray(R.array.windows_variants);
        lp.setSummary(s[nWindow]);
        lp.setValueIndex(nWindow);
        bindSummary(lp);

        lp = findPreference("nWindowHalf");
        assert lp != null;
        s = getResources().getStringArray(R.array.window_half_variants);
        lp.setSummary(s[nWindowHalf]);
        lp.setValueIndex(nWindowHalf);
        bindSummary(lp);

    }

    public static void restoreSettings(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        
        nFft = Integer.parseInt(sp.getString("nFft","256"));
        String[] s = context.getResources().getStringArray(R.array.windows_variants);
        String s0 = sp.getString("nWindow",s[2]);
        nWindow = 1;
        for (int i=0; i<s.length; i++)
            if (s[i].equals(s0)) {
                nWindow = i;
                break;
            }

        SettingsRadarFragment.setClass(m);
        SettingsRadarFragment.restoreSettings(m.getApplication());
        SettingsAntennaFragment.setClass(m);
        SettingsAntennaFragment.restoreSettings(m.getApplication());
        SettingsSelectFragment.setClass(m);
        SettingsSelectFragment.restoreSettings(m.getApplication());
    }
}
