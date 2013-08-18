package com.dsvoronin.grindfm;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dsvoronin.grindfm.network.OkHttpStack;

public class App extends Application {

    private RequestQueue queue;

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        queue = Volley.newRequestQueue(this, new OkHttpStack());
    }

    @Override
    public void onTerminate() {
        instance = null;
        super.onTerminate();
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public static App getApp() {
        return instance;
    }
}

