package com.dsvoronin.grindfm.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:18
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}
