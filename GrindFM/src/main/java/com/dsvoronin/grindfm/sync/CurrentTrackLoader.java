package com.dsvoronin.grindfm.sync;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.dsvoronin.grindfm.App;
import com.dsvoronin.grindfm.entities.Track;

import java.io.IOException;

public class CurrentTrackLoader extends AsyncTaskLoader<Track> {

    private static final String TAG = "CurrentTrackLoader";

    private GrindService grindService;

    public CurrentTrackLoader(Context context) {
        super(context);
        grindService = App.fromContext(context).getGrindService();
    }

    @Override
    public Track loadInBackground() {
        try {
            return grindService.getCurrentSong().execute().body();
        } catch (IOException e) {
            Log.e(TAG, "Can't load current song", e);
            return null;
        }
    }
}
