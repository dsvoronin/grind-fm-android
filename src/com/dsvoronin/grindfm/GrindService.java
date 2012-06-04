package com.dsvoronin.grindfm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.dsvoronin.grindfm.activity.MainActivity;
import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.IceCastScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class GrindService extends Service {

    private final String TAG = "GRIND-SERVICE";

    private static final int NOTIFICATION_ID = 6;

    private MediaPlayer player;

    private boolean playing = false;

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(TAG, "Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!playing) {
            sendCommand(ServiceHandler.COMMAND_PROGRESS);
            start();
            Log.d(TAG, "Started");
        } else {
            Log.d(TAG, "Already playing");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroyed");
        notificationManager.cancel(NOTIFICATION_ID);
        player.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void start() {

        Log.d(TAG, "Player Started");

        try {
            player = new MediaPlayer();
            player.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_ogg)));
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Media prepared");

                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

                    player.start();
                    sendCommand(ServiceHandler.COMMAND_START);

                    String info = getInfo();

                    sendMessage(info);
                    showNotification(info);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.e(TAG, "ERROR! code:" + i + "," + i1);
                    stop();
                    start();
                    return false;
                }
            });

            player.prepareAsync();

            playing = true;
        } catch (IOException e) {
            Log.e(TAG, "Error while loading url", e);
            playing = false;
            stopSelf();
        }
    }

    private void stop() {

        Log.d(TAG, "Player Stopped");
        sendCommand(ServiceHandler.COMMAND_STOP);

        notificationManager.cancel(NOTIFICATION_ID);
        playing = false;
        player.release();
    }

    final PhoneStateListener phoneListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "Someone's calling. Stop playback");
                    player.stop();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "Call ended. Restart playback");
                    player.start();
                    break;
                default:
                    Log.d(TAG, "Unknown phone state = " + state);
            }
        }
    };

    /**
     * show user an ongoing_event notification
     *
     * @param info artist/song pair
     */
    private void showNotification(String info) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.cat_status_bar);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.cat_status_bar_large));
        builder.setTicker(getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(info);
        builder.setOngoing(true);

        notificationManager.notify(NOTIFICATION_ID, builder.getNotification());

        Log.d(TAG, "Notification shown");
    }

    private String getInfo() {
        Scraper scraper = new IceCastScraper();
        List<Stream> streams;
        try {
            URI streamURI = new URI(getString(R.string.radio_stream_url_ogg));
            streams = scraper.scrape(streamURI);
            if (streams != null) {
                for (Stream stream : streams) {
                    if (stream.getUri().equals(streamURI)) {
                        if (!stream.getCurrentSong().equals("")) {
                            return stream.getCurrentSong();
                        }
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

    private synchronized void sendCommand(int m) {
        Log.d(TAG, "Sending command N: " + m);
        Intent intent = new Intent("service-intent");
        intent.putExtra("service-command", m);
        this.sendBroadcast(intent);
    }

    private synchronized void sendMessage(String info) {
        Log.d(TAG, "Sending message N: " + info);
        Intent intent = new Intent("service-intent");
        intent.putExtra("service-message", info);
        sendBroadcast(intent);
    }
}
