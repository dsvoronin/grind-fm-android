package com.dsvoronin.grindfm;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GrindFM.MainActivity";

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new NewsFragment())
                    .add(R.id.bottom_sheet, new PlayerFragment())
                    .commit();
        }
    }
}
