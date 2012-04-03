package com.dsvoronin.grindfm.task;

import android.content.Context;
import android.util.Log;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.model.Video;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VideoTask extends BaseTask {

    private static final String TAG = VideoTask.class.getSimpleName();

    private static final String YOUTUBE_API_LINK_ALL_VIDEOS =
            "http://gdata.youtube.com/feeds/api/users/%CHANNEL%/uploads?" +
                    "v=2&" +
                    "alt=jsonc&" +
                    "orderby=published";

    private String query;

    public VideoTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
        String youtubeChannel = mContext.getString(R.string.youtube_channel);
        query = YOUTUBE_API_LINK_ALL_VIDEOS.replace("%CHANNEL%", youtubeChannel);
    }

    @Override
    protected ArrayList processAsync(String... url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(query);
        HttpResponse r = client.execute(get);
        int status = r.getStatusLine().getStatusCode();

        Log.d(TAG, String.valueOf(status));

        HttpEntity e = r.getEntity();
        String data = EntityUtils.toString(e);

        JSONObject json = new JSONObject(data);
        JSONObject dataObject = json.getJSONObject("data");
        JSONArray items = dataObject.getJSONArray("items");

        ArrayList<Video> result = new ArrayList<Video>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject videoObject = items.getJSONObject(i);

            Video video = new Video();
            video.setTitle(videoObject.getString("title"));
            video.setDate(videoObject.getString("uploaded"));
            video.setThumb(videoObject.getJSONObject("thumbnail").getString("hqDefault"));
            video.setUrl(videoObject.getString("id"));

            result.add(video);
        }

        return result;
    }

    @Override
    protected void afterTaskActions() {
    }
}
