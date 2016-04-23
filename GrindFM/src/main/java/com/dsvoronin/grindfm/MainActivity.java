package com.dsvoronin.grindfm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dsvoronin.grindfm.sync.SyncUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GrindFM.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SyncUtils.createSyncAccount(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new NewsFragment())
                    .add(R.id.bottom_sheet, new PlayerFragment())
                    .commit();
        }
    }
}
