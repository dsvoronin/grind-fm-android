package com.dsvoronin.grindfm.task;

import android.util.Log;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.model.RequestSong;
import com.dsvoronin.grindfm.util.GrindHttpClientException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.HttpGet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dsvoronin
 */
public class RequestSearchTask extends BackgroundHttpTask<RequestSong> {

    private static final String TAG = "Grind.RequestSearchTask";

    public RequestSearchTask(HttpListActivity<RequestSong> requestSongHttpListActivity) {
        super(requestSongHttpListActivity);
    }

    @Override
    protected ArrayList<RequestSong> processAsync(String... url) throws GrindHttpClientException {
        String requestURL = activity.getString(R.string.request_search_url) + url[0].trim();

        Log.d(TAG, "requestURL = " + requestURL);

        String response = grindHttpClient.request(new HttpGet(requestURL));

        Log.d(TAG, "response = " + response);

        String reqShow = response.substring(response.indexOf('(') + 1, response.lastIndexOf(')'));

        GsonBuilder builder = new GsonBuilder();
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(reqShow).getAsJsonObject();
        JsonArray result = object.get("result").getAsJsonArray();

        Type collectionType = new TypeToken<List<RequestSong>>() {
        }.getType();
        return builder.create().fromJson(result, collectionType);
    }
}
