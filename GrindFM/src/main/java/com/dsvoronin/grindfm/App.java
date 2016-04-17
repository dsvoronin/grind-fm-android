package com.dsvoronin.grindfm;

import android.app.Application;
import android.content.Context;

import com.dsvoronin.grindfm.network.RequestManager;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class App extends Application {

    private OkHttpClient okHttpClient;
    private Picasso picasso;

    public static App fromContext(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RequestManager.init(getApplicationContext());

        okHttpClient = new OkHttpClient.Builder()
                .build();

        picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Picasso getPicasso() {
        return picasso;
    }
}
