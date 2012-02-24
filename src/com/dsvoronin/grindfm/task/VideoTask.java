package com.dsvoronin.grindfm.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.dsvoronin.grindfm.Progressable;
import com.dsvoronin.grindfm.adapter.VideoAdapter;
import com.dsvoronin.grindfm.model.Video;
import com.dsvoronin.grindfm.util.YouTubeUtil;

import java.util.ArrayList;

public class VideoTask extends AsyncTask<String, Void, ArrayList<Video>> {

    private static final String TAG = VideoTask.class.getSimpleName();

    private Activity mContext;
    private VideoAdapter mAdapter;

    private Progressable progressable;

    public VideoTask(Activity context, VideoAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        if (progressable != null) {
            progressable.taskStarted();
        }
    }

    @Override
    protected ArrayList<Video> doInBackground(String... strings) {
        try {
            YouTubeUtil youTubeUtil = new YouTubeUtil();
            return new ArrayList<Video>(youTubeUtil.getPlayList(strings[0]));
        } catch (Exception e) {
            Log.e(TAG, "Error while parsing youtube playlist " + strings[0], e);
            return new ArrayList<Video>();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Video> videos) {
        if (progressable != null) {
            progressable.taskComplete();
        }
        if (videos.size() > 0) {
            mAdapter.replaceContent(videos);
            mAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(mContext, "Не удалось загрузить новости", Toast.LENGTH_SHORT).show();
        }
    }

    public void setProgressable(Progressable progressable) {
        this.progressable = progressable;
    }
}
