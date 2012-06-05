package com.dsvoronin.grindfm.task;

import android.util.Log;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.model.Video;
import com.dsvoronin.grindfm.util.GrindHttpClientException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.HttpGet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoTask extends BackgroundHttpTask<Video> {

    private static final String TAG = "Grind.VideoTask";

    public VideoTask(HttpListActivity<Video> videoHttpListActivity) {
        super(videoHttpListActivity);
    }

    @Override
    protected ArrayList<Video> processAsync(String... url) throws GrindHttpClientException {
        String requestURL = activity.getString(R.string.youtube_url).replace("%USER%", activity.getString(R.string.youtube_user));
        Log.d(TAG, "requestURL = " + requestURL);

        String response = grindHttpClient.request(new HttpGet(requestURL));
        Log.d(TAG, "response = " + response);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new Video.DateDeserializer());
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response).getAsJsonObject();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray array = data.get("items").getAsJsonArray();
        Type collectionType = new TypeToken<List<Video>>() {
        }.getType();
        return builder.create().fromJson(array, collectionType);
    }
}
