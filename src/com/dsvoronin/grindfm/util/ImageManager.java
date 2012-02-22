package com.dsvoronin.grindfm.util;

import android.content.Context;
import android.view.View;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;

import java.io.File;

public class ImageManager {

    private static ImageManager instance = new ImageManager();

    private AQuery aq;

    private boolean fileCache;

    private ImageManager() {
    }

    public void init(Context context, String externalStoragePath) {
        aq = new AQuery(context);
        AQUtility.setCacheDir(new File(externalStoragePath));
    }

    public void setFileCache(boolean fileCache) {
        this.fileCache = fileCache;
    }

    /**
     * Используется для загрузки картинок из веб
     *
     * @param imageView   view который отобразит картинку
     * @param progessView view progress bar
     * @param url         url картинки
     */
    public void displayImage(View imageView, View progessView, String url) {
        aq.id(imageView).progress(progessView).image(url, true, fileCache);
    }

    /**
     * Используется для загрузки картинок из веб
     *
     * @param imageView view который отобразит картинку
     * @param url       url картинки
     */
    public void displayImage(View imageView, String url) {
        aq.id(imageView).image(url, true, fileCache);
    }

    public void displayImage(View view, int resId) {
        aq.id(view).image(resId);
    }

    public static ImageManager getInstance() {
        return instance;
    }
}
