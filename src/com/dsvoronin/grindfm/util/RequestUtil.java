package com.dsvoronin.grindfm.util;

import android.util.Log;
import com.dsvoronin.grindfm.model.RequestSong;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RequestUtil {

    private static final String REQUEST_SEARCH_URL = "http://media.goha.ru/radio/req2.php?anything=";
    private static final String REQUEST_URL = "http://media.goha.ru/radio/req2.php?int_id=";

    public static List<RequestSong> search(String str) throws IOException, JSONException {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(REQUEST_SEARCH_URL + str);
        HttpResponse r = client.execute(get);
        int status = r.getStatusLine().getStatusCode();
        HttpEntity e = r.getEntity();
        String data = EntityUtils.toString(e);
        String reqShow = data.substring(data.indexOf('(') + 1, data.lastIndexOf(')'));
        JSONObject json = new JSONObject(reqShow);
        JSONArray items = json.getJSONArray("result");

        List<RequestSong> songList = new ArrayList<RequestSong>();

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

    public static String request(long songId) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(REQUEST_URL + songId);
            HttpResponse r = client.execute(get);
            int status = r.getStatusLine().getStatusCode();
            HttpEntity e = r.getEntity();
            String data = EntityUtils.toString(e);
            String reqShow = data.substring(data.indexOf('(') + 1, data.lastIndexOf(')'));
            JSONObject json = new JSONObject(reqShow);
            return json.getString("error");
        } catch (Exception e) {
            Log.e("Request", "Error", e);
            return "Ошибка сервиса";
        }
    }
}
