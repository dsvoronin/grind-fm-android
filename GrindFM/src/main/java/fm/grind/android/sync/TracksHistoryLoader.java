package fm.grind.android.sync;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import fm.grind.android.App;
import fm.grind.android.entities.TrackInList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TracksHistoryLoader extends AsyncTaskLoader<List<TrackInList>> {

    private static final String TAG = "TracksHistoryLoader";

    private GrindService grindService;

    public TracksHistoryLoader(Context context) {
        super(context);
        grindService = App.Companion.fromContext(context).getGrindService();
    }

    @Override
    public List<TrackInList> loadInBackground() {
        try {
            return grindService.getLastPlayedTracks().execute().body();
        } catch (IOException e) {
            Log.e(TAG, "Can't load last played tracks", e);
            return Collections.emptyList();
        }
    }
}