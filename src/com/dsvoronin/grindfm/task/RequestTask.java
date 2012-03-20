package com.dsvoronin.grindfm.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class RequestTask extends AsyncTask<Integer, Void, String> {

    private static final String TAG = RequestTask.class.getSimpleName();

    private static final String REQUEST_URL = "http://media.goha.ru/radio/req2.php?int_id=";

    private Activity context;

    public RequestTask(Activity context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Integer... objects) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(REQUEST_URL + objects[0]);
            HttpResponse r = client.execute(get);

            int status = r.getStatusLine().getStatusCode();
            Log.d(TAG, String.valueOf(status));

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

    @Override
    protected void onPostExecute(String o) {
        Toast.makeText(context, o, Toast.LENGTH_SHORT).show();
    }
}
