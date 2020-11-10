package com.polant.projectsport.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.polant.projectsport.R;


public class PreferencesOldActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_preferences);
    }

}
