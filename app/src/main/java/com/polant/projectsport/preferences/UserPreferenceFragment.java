package com.polant.projectsport.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.polant.projectsport.R;


public class UserPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_preferences);
    }
}
