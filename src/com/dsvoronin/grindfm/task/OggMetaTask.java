package com.dsvoronin.grindfm.task;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.dsvoronin.grindfm.GrindNotification;
import com.dsvoronin.grindfm.GrindService;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.util.GrindHttpClient;
import com.dsvoronin.grindfm.util.GrindHttpClientException;
import org.apache.http.client.methods.HttpGet;

/**
 * User: dsvoronin
 * Date: 05.06.12
 * Time: 15:07
 */
public class OggMetaTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "Grind.OggMetaTask";

    private GrindService service;

    public OggMetaTask(GrindService service) {
        this.service = service;
    }

    @Override
    protected String doInBackground(String... strings) {
        GrindHttpClient httpClient = new GrindHttpClient(service.getResources().getInteger(R.integer.connection_timeout), service.getResources().getInteger(R.integer.socket_timeout));
        try {
            String response = httpClient.request(new HttpGet(strings[0]));
            return response.substring(response.indexOf(",,") + 2);
        } catch (GrindHttpClientException e) {
            Log.e(TAG, "Error while parsing icecast", e);
            return "Нет данных";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (service == null) {
            return;
        }

        if (s == null) {
            return;
        }

        String oldMeta = service.getOldMeta();
        if (oldMeta == null) {
            service.setOldMeta(s);
        } else {
            if (!oldMeta.equals(s)) {
                Log.d(TAG, "Sending message N: " + s);
                Intent intent = new Intent("service-intent");
                intent.putExtra("service-message", s);
                service.sendBroadcast(intent);
                NotificationManager notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(GrindService.NOTIFICATION_ID, new GrindNotification(s).buildNotification());
                service.setOldMeta(s);
            }
        }
    }
}
