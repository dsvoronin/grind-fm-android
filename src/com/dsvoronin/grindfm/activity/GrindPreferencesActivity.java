package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.dsvoronin.grindfm.R;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 19:01
 */
public class GrindPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
