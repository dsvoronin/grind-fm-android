package com.dsvoronin.grindfm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class GrindService extends Service {

    private static final String TAG = GrindService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 6;

    private MediaPlayer player;

    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        setForeground(true);

        try {
            player = new MediaPlayer();
            player.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_ogg)));
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                }
            });
            player.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error while loading url", e);
            onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
        player.stop();
    }

    private void showNotification() {
        Notification notification = new Notification(R.drawable.cat, getString(R.string.app_name), System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, GrindActivity.class), 0);
        notification.setLatestEventInfo(this, "GRIND", "GRIND", pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
