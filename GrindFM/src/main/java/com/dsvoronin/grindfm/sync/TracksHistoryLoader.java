package com.dsvoronin.grindfm.sync;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.dsvoronin.grindfm.App;
import com.dsvoronin.grindfm.entities.TrackListItem;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TracksHistoryLoader extends AsyncTaskLoader<List<TrackListItem>> {

    private static final String TAG = "TracksHistoryLoader";

    private GrindService grindService;

    public TracksHistoryLoader(Context context) {
        super(context);
        grindService = App.fromContext(context).getGrindService();
    }

    @Override
    public List<TrackListItem> loadInBackground() {
        try {
            return grindService.getLastPlayedTracks().execute().body();
        } catch (IOException e) {
            Log.e(TAG, "Can't load last played tracks", e);
            return Collections.emptyList();
        }
    }
}