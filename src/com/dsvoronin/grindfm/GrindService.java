package com.dsvoronin.grindfm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.dsvoronin.grindfm.activity.NewsActivity;
import net.moraleboost.streamscraper.ScrapeException;
import net.moraleboost.streamscraper.Scraper;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.scraper.IceCastScraper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class GrindService extends Service {

    private static final int NOTIFICATION_ID = 6;

    private MediaPlayer player;

    private boolean playing = false;

    private NotificationManager notificationManager;

    private final IGrindPlayer.Stub binder = new IGrindPlayer.Stub() {

        @Override
        public String getInfo() throws RemoteException {
            return GrindService.this.getInfo();
        }

        @Override
        public boolean playing() throws RemoteException {
            return playing;
        }

        @Override
        public void startAudio() throws RemoteException {
            start();
        }

        @Override
        public void stopAudio() throws RemoteException {
            stop();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        String TAG = "GrindService:onBind";
        Log.d(TAG, "START");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (playing) {
            sendMessage(getInfo());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
        player.release();
    }

    private void start() {
        String TAG = "GrindService:start";
        Log.d(TAG, "STARTED");

        try {
            player = new MediaPlayer();
            player.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_ogg)));
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

                    player.start();

                    String info = getInfo();

                    sendMessage(info);
                    showNotification(info);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    String TAG = "GrindService:start:MediaPlayer:onError";
                    Log.e(TAG, "ERROR!");
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
            onDestroy();
        }
    }

    private void stop() {
        notificationManager.cancel(NOTIFICATION_ID);
        playing = false;
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, NewsActivity.class), 0);
        notification.setLatestEventInfo(this, appName, info, pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void sendMessage(String info) {
        Intent intent = new Intent(getString(R.string.service_intent));
        intent.putExtra(getString(R.string.service_intent_info), info);
        sendBroadcast(intent);
    }

    private String getInfo() {
        String TAG = "GrindService:getInfo";

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
}
