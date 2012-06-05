package com.dsvoronin.grindfm.task;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.util.GrindHttpClient;
import com.dsvoronin.grindfm.util.GrindHttpClientException;

import java.util.ArrayList;

public abstract class BackgroundHttpTask<T> extends AsyncTask<String, Void, ArrayList<T>> {

    private static final String TAG = "Grind.BackgroundHttpTask";

    protected HttpListActivity<T> activity;

    protected GrindHttpClient grindHttpClient;

    private String grindHttpClientError;

    public BackgroundHttpTask(HttpListActivity<T> activity) {
        this.activity = activity;

        int connectionTimeout = activity.getResources().getInteger(R.integer.connection_timeout);
        int socketTimeout = activity.getResources().getInteger(R.integer.socket_timeout);

        grindHttpClient = new GrindHttpClient(connectionTimeout, socketTimeout);
    }

    @Override
    protected void onPreExecute() {
        activity.displayProgress();
    }

    @Override
    protected ArrayList<T> doInBackground(String... urls) {
        try {
            return processAsync(urls);
        } catch (GrindHttpClientException e) {
            Log.e(TAG, "Http error", e);
            grindHttpClientError = e.getMessage();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<T> list) {

        if (activity == null) {
            Log.w(TAG, "activity gone - task skipped");
            return;
        }

        if (grindHttpClientError != null) {
            Toast.makeText(activity, grindHttpClientError, Toast.LENGTH_SHORT).show();
            activity.displayGotError();
            return;
        }

        if (list.size() == 0) {
            Toast.makeText(activity, "Ничего не нашлось :(", Toast.LENGTH_SHORT).show();
            activity.displayGotError();
            return;
        }

        activity.displayOk();
        activity.populateAdapter(list);
    }

    public void attach(HttpListActivity<T> activity) {
        this.activity = activity;
    }

    public void detach() {
        activity = null;
    }

    protected abstract ArrayList<T> processAsync(String... urls) throws GrindHttpClientException;
}
