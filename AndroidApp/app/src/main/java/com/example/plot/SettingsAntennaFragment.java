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

public class SettingsAntennaFragment extends PreferenceFragmentCompat {
    static MainThread m;
    public static void setClass(MainThread mainThread){
        m = mainThread;
    }
    static boolean isLpdaTxVisible = false ,isLpdaRxVisible = false,
            isSpiralTxVisible = false ,isSpiralRxVisible = false;

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

                switch (preference.getKey()){
                    case "nAntennaTx":
                        if (m.nAntennaTx != lp.findIndexOfValue(stringValue))
                            SettingsFragment.isSettingsChanged = true;
                        m.nAntennaTx = (char)lp.findIndexOfValue(stringValue);
                        break;
                    case "nAntennaRx":
                        if (m.nAntennaRx != lp.findIndexOfValue(stringValue))
                            SettingsFragment.isSettingsChanged = true;
                        m.nAntennaRx = (char)lp.findIndexOfValue(stringValue);
                        break;
                }
                lp.setValueIndex(lp.findIndexOfValue(stringValue));
                preference.setSummary(stringValue);
                updatePreferences();
            }
            else if (preference instanceof CheckBoxPreference){

            }
            else if (preference instanceof EditTextPreference){
                float temp;
                EditTextPreference ep = (EditTextPreference) preference;
                String stringValue = newValue.toString();
                ep.setSummary(newValue.toString());
                switch (ep.getKey()) {
                    case "editMinLpdaTxWidth":
                        temp = Float.parseFloat(stringValue);
                        if (m.antTxLpdaMin != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.antTxLpdaMin = temp;
                        break;
                    case "editMaxLpdaTxWidth":
                        temp = Float.parseFloat(stringValue);
                        if (m.antTxLpdaMax != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.antTxLpdaMax = temp;
                        break;
                    case "editMinLpdaRxWidth":
                        temp = Float.parseFloat(stringValue);
                        if (m.antRxLpdaMin != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.antRxLpdaMin = temp;
                        break;
                    case "editMaxLpdaRxWidth":
                        temp = Float.parseFloat(stringValue);
                        if (m.antRxLpdaMax != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.antRxLpdaMax = temp;
                        break;
                    case "editSpiralTxWidth":
                        temp = Float.parseFloat(stringValue);
                        if (m.antTxSpiral != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.antTxSpiral = temp;
                        break;
                    case "editSpiralRxWidth":
                        temp = Float.parseFloat(stringValue);
                        if (m.antRxSpiral != temp)
                            SettingsFragment.isSettingsChanged = true;
                        m.antRxSpiral = temp;
                        break;
                }
                changeInfo();
            }
            return false;
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_antenna,rootKey);

        ListPreference lp;
        EditTextPreference ep;
        String[] s;

        lp = findPreference("nAntennaTx");
        assert lp != null;
        s = getResources().getStringArray(R.array.antenna_variants);
        lp.setSummary(s[m.nAntennaTx]);
        lp.setValueIndex(m.nAntennaTx);
        lp.setOnPreferenceChangeListener(listener);

        lp = findPreference("nAntennaRx");
        assert lp != null;
        s = getResources().getStringArray(R.array.antenna_variants);
        lp.setSummary(s[m.nAntennaRx]);
        lp.setValueIndex(m.nAntennaRx);
        lp.setOnPreferenceChangeListener(listener);

        updatePreferences();
        changeInfo();
    }

    void updatePreferences(){

        if(m.nAntennaTx==MainThread.ANTENNA_LPDA) {
            isLpdaTxVisible = true;
            isSpiralTxVisible = false;
        }
        else if(m.nAntennaTx==MainThread.ANTENNA_SPIRAL) {
            isLpdaTxVisible = false;
            isSpiralTxVisible = true;
        }
        else{
            isLpdaTxVisible = false;
            isSpiralTxVisible = false;
        }
        if(m.nAntennaRx==MainThread.ANTENNA_LPDA) {
            isLpdaRxVisible = true;
            isSpiralRxVisible = false;
        }
        else if(m.nAntennaTx==MainThread.ANTENNA_SPIRAL) {
            isLpdaRxVisible = false;
            isSpiralRxVisible = true;
        }
        else{
            isLpdaRxVisible = false;
            isSpiralRxVisible = false;
        }

        EditTextPreference ep;
        ep = findPreference("editMinLpdaTxWidth");
        assert ep != null;
        ep.setText(Float.toString(m.antTxLpdaMin));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.antTxLpdaMin +" m");
        ep.setVisible(isLpdaTxVisible);

        ep = findPreference("editMaxLpdaTxWidth");
        assert ep != null;
        ep.setText(Float.toString(m.antTxLpdaMax));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.antTxLpdaMax +" m");
        ep.setVisible(isLpdaTxVisible);

        ep = findPreference("editSpiralTxWidth");
        assert ep != null;
        ep.setText(Float.toString(m.antTxSpiral));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.antTxSpiral +" m");
        ep.setVisible(isSpiralTxVisible);


        ep = findPreference("editMinLpdaRxWidth");
        assert ep != null;
        ep.setText(Float.toString(m.antRxLpdaMin));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.antRxLpdaMin +" m");
        ep.setVisible(isLpdaRxVisible);

        ep = findPreference("editMaxLpdaRxWidth");
        assert ep != null;
        ep.setText(Float.toString(m.antRxLpdaMax));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.antRxLpdaMax +" m");
        ep.setVisible(isLpdaRxVisible);

        ep = findPreference("editSpiralRxWidth");
        assert ep != null;
        ep.setText(Float.toString(m.antRxSpiral));
        ep.setOnPreferenceChangeListener(listener);
        ep.setOnBindEditTextListener(digital);
        ep.setSummary(m.antRxSpiral +" m");
        ep.setVisible(isSpiralRxVisible);
    }

    public void changeInfo(){
        Preference p = findPreference("prefAntennaInfo");
        assert p!=null;
        String[] s = m.getApplication().getApplicationContext().getResources().getStringArray(R.array.antenna_variants);
        String sTx = s[m.nAntennaTx];
        String sRx = s[m.nAntennaRx];
        StringBuilder string = new StringBuilder();
        string.append("Transmission antenna is ").append(sTx).append("\n");
        switch (m.nAntennaTx){
            case MainThread.ANTENNA_DIPOLE:
                break;
            case MainThread.ANTENNA_LPDA:
                string.append("Frequency range is ");
                string.append(3e2f/m.antTxLpdaMax/2);
                string.append(" to ");
                string.append(3e2f/m.antTxLpdaMin/2);
                string.append(" MHz\n");
                break;
            case MainThread.ANTENNA_SPIRAL:
                string.append("Frequency is above ");
                string.append(3e2f/m.antTxSpiral);
                string.append(" MHz\n");
                break;
        }
        string.append("\n");
        string.append("Reception antenna is ").append(sRx).append("\n");
        switch (m.nAntennaRx){
            case MainThread.ANTENNA_DIPOLE:
                break;
            case MainThread.ANTENNA_LPDA:
                string.append("Frequency range is ");
                string.append(3e2f/m.antRxLpdaMax/2);
                string.append(" to ");
                string.append(3e2f/m.antRxLpdaMin/2);
                string.append(" MHz\n");
                break;
            case MainThread.ANTENNA_SPIRAL:
                string.append("Frequency is above ");
                string.append(3e2f/m.antRxSpiral);
                string.append(" MHz\n");
                break;
        }

        p.setSummary(string.toString());
    }

    public static void restoreSettings(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String[] s = context.getResources().getStringArray(R.array.antenna_variants);
        String s0 = sp.getString("nAntennaTx",s[MainThread.ANTENNA_LPDA]);
        m.nAntennaTx = 1;
        for (char i=0; i<s.length; i++)
            if (s[i].equals(s0)) {
                m.nAntennaTx = i;
                break;
            }

        s0 = sp.getString("nAntennaRx",s[MainThread.ANTENNA_LPDA]);
        m.nAntennaRx = 1;
        for (char i=0; i<s.length; i++)
            if (s[i].equals(s0)) {
                m.nAntennaRx = i;
                break;
            }

        m.antTxLpdaMin = Float.parseFloat(sp.getString("editMinLpdaTxWidth","0.01"));
        m.antTxLpdaMax = Float.parseFloat(sp.getString("editMaxLpdaTxWidth","0.15"));
        m.antRxLpdaMin = Float.parseFloat(sp.getString("editMinLpdaRxWidth","0.01"));
        m.antRxLpdaMax = Float.parseFloat(sp.getString("editMaxLpdaRxWidth","0.15"));
        m.antTxSpiral = Float.parseFloat(sp.getString("editSpiralTxWidth","0.3"));
        m.antRxSpiral = Float.parseFloat(sp.getString("editSpiralRxWidth","0.3"));
    }
}
