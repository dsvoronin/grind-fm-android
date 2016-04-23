package com.dsvoronin.grindfm;

import android.app.Application;
import android.content.Context;

import com.dsvoronin.grindfm.sync.GrindService;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class App extends Application {

    private Picasso picasso;

    private GrindService grindService;

    public static App fromContext(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();

        grindService = new Retrofit.Builder()
                .baseUrl("http://grind.fm")
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
                .create(GrindService.class);
    }

    public Picasso getPicasso() {
        return picasso;
    }

    public GrindService getGrindService() {
        return grindService;
    }
}
