package com.dsvoronin.grindfm;

import android.app.Application;
import android.graphics.Bitmap;

import com.dsvoronin.grindfm.cache.ImageCacheManager;
import com.dsvoronin.grindfm.network.RequestManager;

public class App extends Application {

    private static App instance;

    public static App getApp() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RequestManager.init(getApplicationContext());
        ImageCacheManager.getInstance().init(getApplicationContext(), "img", (int) (Runtime.getRuntime().freeMemory() / 8), Bitmap.CompressFormat.JPEG, 70, ImageCacheManager.CacheType.MEMORY);
    }

    @Override
    public void onTerminate() {
        instance = null;
        super.onTerminate();
    }
}
