package com.dsvoronin.grindfm;

import android.app.Application;
import android.os.Environment;
import com.dsvoronin.grindfm.util.ImageManager;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "dGlWLTc2OXZKSWlwNWxkZWs1cUZ3Y3c6MQ")
public class GrindApplication extends Application {

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        String cacheDir = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/cache/";
        ImageManager imageManager = ImageManager.getInstance();
        imageManager.init(this, cacheDir);
        imageManager.setFileCache(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
