package com.aarkir.tiles;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call super :
        super.onCreate(savedInstanceState);

        // Set the activity's fragment :
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        private SeekBarPreference columnCountPref;
        private SeekBarPreference maximumAppsPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Get widgets :
            columnCountPref = (SeekBarPreference) this.findPreference("columnCount");
            maximumAppsPref = (SeekBarPreference) this.findPreference("maximumApps");

            // Set listener :
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            // Set seekbar summary :
            setSummary("maximumApps", maximumAppsPref, R.string.maximumApps_summary);
            setSummary("columnCount", columnCountPref, R.string.columnCount_summary);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Set seekbar summary :
            setSummary("maximumApps", maximumAppsPref, R.string.maximumApps_summary);
            setSummary("columnCount", columnCountPref, R.string.columnCount_summary);
        }

        //set the summary under the preference showing the current value
        private void setSummary(String key, SeekBarPreference seekBarPreference, int summaryID) {
            int value = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt(key, 5);
            seekBarPreference.setSummary(this.getString(summaryID).replace("$1", "" + value));
        }
    }
}
