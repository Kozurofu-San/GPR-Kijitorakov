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

public class SettingsSelectFragment extends PreferenceFragmentCompat {
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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_select,rootKey);

        ListPreference lp;
        EditTextPreference ep;

    }

    public static void restoreSettings(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

    }
}
