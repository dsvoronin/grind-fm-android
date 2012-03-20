package com.dsvoronin.grindfm.task;

import android.content.Context;
import android.util.Log;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.model.RequestSong;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RequestSearchTask extends BaseTask {

    private static final String TAG = RequestSearchTask.class.getSimpleName();

    private static final String REQUEST_SEARCH_URL = "http://media.goha.ru/radio/req2.php?anything=";

    public RequestSearchTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
    }

    @Override
    protected ArrayList processAsync(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(REQUEST_SEARCH_URL + url);
        HttpResponse r = client.execute(get);

        int status = r.getStatusLine().getStatusCode();
        Log.d(TAG, String.valueOf(status));

        HttpEntity e = r.getEntity();
        String data = EntityUtils.toString(e);
        String reqShow = data.substring(data.indexOf('(') + 1, data.lastIndexOf(')'));
        JSONObject json = new JSONObject(reqShow);
        JSONArray items = json.getJSONArray("result");

        ArrayList<RequestSong> songList = new ArrayList<RequestSong>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject videoObject = items.getJSONObject(i);

            RequestSong song = new RequestSong();
            song.setId(videoObject.getInt("int_id"));
            song.setAlbum(videoObject.getString("album"));
            song.setArtist(videoObject.getString("artist"));
            song.setAvailable(videoObject.get("lastp").toString().equals("null"));
            song.setTitle(videoObject.getString("title"));
            songList.add(song);
        }

        return songList;
    }

    @Override
    protected void afterTaskActions() {
    }
}
