package com.dsvoronin.grindfm;

import android.app.Application;
import android.content.Context;

import com.dsvoronin.grindfm.sync.GrindService;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class App extends Application {

    private Picasso picasso;

    private GrindService grindService;

    /**
     * 300mb cache
     */
    private static final int CACHE_SIZE = 1024 * 1024 * 300;

    public static App fromContext(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        LeakCanary.install(this);

        File cacheDir = getDir("okhttp_cache", Context.MODE_PRIVATE);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .cache(new Cache(cacheDir, CACHE_SIZE))
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
