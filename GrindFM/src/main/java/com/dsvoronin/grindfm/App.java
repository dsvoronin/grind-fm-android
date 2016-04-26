package com.dsvoronin.grindfm;

import android.app.Application;
import android.content.Context;

import com.dsvoronin.grindfm.sync.GrindService;
import com.dsvoronin.grindfm.utils.CurrentTrackConverterFactory;
import com.dsvoronin.grindfm.utils.LastTracksConverterFactory;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
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

    private Tracker tracker;

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
                .addConverterFactory(CurrentTrackConverterFactory.create())
                .addConverterFactory(LastTracksConverterFactory.create())
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

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
