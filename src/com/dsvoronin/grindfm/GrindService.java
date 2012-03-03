package com.dsvoronin.grindfm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.IceCastScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

        setForeground(true);

        try {
            player = new MediaPlayer();
            player.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_mp3)));
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

                    player.start();

                    String info = getInfo();

                    announceNewSong(info);
                    showNotification(info);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.e(TAG, "ERROR!");
                    stopSelf();
                    return false;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    String info = getInfo();
                    announceNewSong(info);
                    showNotification(info);
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
        player.release();
    }

    final PhoneStateListener phoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            String TAG = "PhoneStateListener";

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "Someone's calling. Stop playback");
                    player.stop();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    player.start();
                    break;
                default:
                    Log.d(TAG, "Unknown phone state = " + state);
            }
        }
    };

    private void showNotification(String info) {
        String appName = getString(R.string.app_name);
        Notification notification = new Notification(R.drawable.cat, appName, System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, GrindActivity.class), 0);
        notification.setLatestEventInfo(this, appName, info, pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void announceNewSong(String info) {
        Intent intent = new Intent(getString(R.string.service_intent));
        intent.putExtra(getString(R.string.service_intent_info), info);
        sendBroadcast(intent);
    }

    private String getInfo() {
        Scraper scraper = new IceCastScraper();
        List<Stream> streams;
        try {
            streams = scraper.scrape(new URI(getString(R.string.radio_stream_url_ogg)));
            if (streams != null) {
                for (Stream stream : streams) {
                    if (!stream.getCurrentSong().equals("")) {
                        return stream.getCurrentSong();
                    }
                }
            }
        } catch (ScrapeException e) {
            Log.e(TAG, "Error while parsing icecast", e);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Error while parsing icecast", e);
        }
        return "";
    }
}
