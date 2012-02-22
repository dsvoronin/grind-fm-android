package com.dsvoronin.grindfm.util;

import com.dsvoronin.grindfm.model.Video;
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

public class YouTubeUtil {

    public static final String YOUTUBE_VIDEO = "http://www.youtube.com/watch?v=";

    private static final String YOUTUBE_API_LINK_VIDEO = "http://gdata.youtube.com/feeds/api/videos/";
    private static final String YOUTUBE_API_LINK_PLAYLIST = "http://gdata.youtube.com/feeds/api/playlists/";


    public YouTubeUtil() {
    }

    public List<Video> getPlayList(String playlistId) throws JSONException, IOException {

        StringBuilder url = new StringBuilder(YOUTUBE_API_LINK_PLAYLIST + playlistId + "?v=2&alt=jsonc");

        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url.toString());
        HttpResponse r = client.execute(get);
        int status = r.getStatusLine().getStatusCode();
        HttpEntity e = r.getEntity();
        String data = EntityUtils.toString(e);
        JSONObject json = new JSONObject(data);
        JSONObject dataObject = json.getJSONObject("data"); // this is the "data": { } part
        JSONArray items = dataObject.getJSONArray("items"); // this is the "items: [ ] part

        List<Video> result = new ArrayList<Video>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject videoObject = items.getJSONObject(i).getJSONObject("video");

            Video video = new Video();
            video.setTitle(videoObject.getString("title"));
            video.setDate(videoObject.getString("uploaded"));
            video.setThumb(videoObject.getJSONObject("thumbnail").getString("hqDefault"));
            video.setUrl(videoObject.getString("id"));

            result.add(video);
        }

        return result;
    }
}
