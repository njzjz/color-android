package com.njzjz.color;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar ab = getSupportActionBar();
        //ab.setDisplayShowHomeEnabled(true);
        //ab.setDisplayHomeAsUpEnabled(true);
        //ab.setHomeButtonEnabled(true);

        PreferenceFragment fragment = new PreferenceFragment() {
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState) {
                View view = super.onCreateView(inflater, container, savedInstanceState);
                return view;
            }

            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.settings);
                EditTextPreference sizeEdit = (EditTextPreference) findPreference("size");
                sizeEdit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String size = newValue.toString();
                        if (isNumeric(size)) {
                            PreferenceUtils.setPrefString(getApplicationContext(), "size", size);
                            EditTextPreference pKwEditText = (EditTextPreference) findPreference("size");
                            pKwEditText.setSummary(size);
                            return true;
                        } else return false;
                    }
                });
                String size = PreferenceUtils.getPrefString(getApplicationContext(), "size", "50");
                sizeEdit.setText(size);
                sizeEdit.setSummary(size);
            }
        };
        getFragmentManager().beginTransaction()
                .replace(R.id.include_settings_container, fragment)
                .commit();
    }
    public static boolean isNumeric(String s){	try{	Double.parseDouble(s);return true;	}catch (Exception e){return false;}}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
