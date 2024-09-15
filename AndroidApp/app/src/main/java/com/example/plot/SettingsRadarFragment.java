package com.example.plot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsRadarFragment extends PreferenceFragmentCompat {
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

    private final Preference.OnPreferenceChangeListener
            listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            if (preference instanceof ListPreference){
                String stringValue = newValue.toString();
                ListPreference lp = (ListPreference) preference;

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
                switch (ep.getKey()){
                    case "editFmin":
                        temp = Short.parseShort(stringValue);
                        if (m.fmin != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.fmin = temp;
                        break;
                    case "editFmax":
                        temp = Short.parseShort(stringValue);
                        if (m.fmax != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.fmax = temp;
                        break;
                    case "editFstep":
                        temp = Short.parseShort(stringValue);
                        if (m.fstep != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.fstep = temp;
                        break;
                    case "editTexp":
                        temp = Short.parseShort(stringValue);
                        if (m.texp != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.texp = temp;
                        break;
                    case "editPower":
                        temp = Short.parseShort(stringValue);
                        if (m.power != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.power = temp;
                        break;
                    case "editPhase":
                        temp = Short.parseShort(stringValue);
                        if (m.phase != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.phase = temp;
                        break;
                }
                changeInfo();
            }
            return false;
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_radar,rootKey);

        Preference p;
        EditTextPreference ep;

        ep = findPreference("editFmin");
        assert ep != null;
        ep.setText(Short.toString(m.fmin));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.fmin +" MHz");

        ep = findPreference("editFmax");
        assert ep != null;
        ep.setText(Short.toString(m.fmax));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.fmax +" MHz");

        ep = findPreference("editFstep");
        assert ep != null;
        ep.setText(Short.toString(m.fstep));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.fstep +" MHz");

        ep = findPreference("editTexp");
        assert ep != null;
        ep.setText(Short.toString(m.texp));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.texp +" ms");

        ep = findPreference("editPower");
        assert ep != null;
        ep.setText(Short.toString(m.power));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.power +" dBm");

        ep = findPreference("editPhase");
        assert ep != null;
        ep.setText(Short.toString(m.phase));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.phase +" deg");

        changeInfo();
    }

    public void changeInfo(){
        Preference p = findPreference("prefRadarInfo");
        assert p!=null;
        int bandwidth = m.fmax-m.fmin;
        float resolution = 1.5e2f/bandwidth;
        float wavelengthMin = 3e2f/m.fmax;
        float wavelengthMax = 3e2f/m.fmin;
        float maxRange = 1.5e2f/ m.fstep;
        p.setSummary("Radar bandwidth is "+bandwidth+" MHz\n" +
                "Resolution is "+resolution+" m\n" +
                "Wavelength range is "+wavelengthMin+" to "+wavelengthMax+" m\n" +
                "Maximum range is "+maxRange+" m");
    }

    public static void restoreSettings(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        m.fmin = Short.parseShort(sp.getString("editFmin","800"));
        m.fmax = Short.parseShort(sp.getString("editFmax", "3000"));
        m.fstep = Short.parseShort(sp.getString("editFstep", "10"));
        m.texp = Short.parseShort(sp.getString("editTexp", "60"));
        m.power = Short.parseShort(sp.getString("editPower", "3"));
        m.phase = Short.parseShort(sp.getString("editPhase", "0"));

    }
}