package com.dsvoronin.grindfm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.dsvoronin.grindfm.activity.NewsActivity;
import com.dsvoronin.grindfm.util.StringUtil;
import com.dsvoronin.grindfm.widget.GrindWidgetProvider;
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
            sendMessage(getResources().getInteger(R.integer.service_intent_message_progress));
            start();
            Log.d(TAG, "Started");
        } else {
            Log.d(TAG, "Already playing");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
        player.release();
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

                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

                    player.start();

                    String info = getInfo();

                    sendMessage(info);
                    showNotification(info);
                    updateWidget(info);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
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

        Log.d(TAG, "Player Stopped");

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

    private void showNotification(String info) {
        Log.d(TAG, "Notification show");

        String appName = getString(R.string.app_name);
        Notification notification = new Notification(R.drawable.cat, appName, System.currentTimeMillis());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, NewsActivity.class), 0);
        notification.setLatestEventInfo(this, appName, info, pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void updateWidget(String info) {
        Log.d(TAG, "Widget update");

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(getApplicationContext());
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.grind_widget);
        views.setTextViewText(R.id.widget_text, StringUtil.widgetString(info));
        views.setImageViewResource(R.id.widget_play, android.R.drawable.ic_media_pause);

        Intent intent = new Intent(getString(R.string.service_intent));
        intent.putExtra(getString(R.string.service_intent_message), getResources().getInteger(R.integer.service_intent_stop));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_play, pendingIntent);
        views.setViewVisibility(R.id.widget_play, View.VISIBLE);
        views.setViewVisibility(R.id.widget_progress_bar, View.GONE);

        ComponentName widgetProvider = new ComponentName(getApplicationContext(), GrindWidgetProvider.class);
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    private void sendMessage(String info) {
        Log.d(TAG, "Running string update");

        Intent intent = new Intent(getString(R.string.service_intent));
        intent.putExtra(getString(R.string.service_intent_info), info);
        sendBroadcast(intent);
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

    private synchronized void sendMessage(int m) {
        Intent i = new Intent(getString(R.string.service_intent));
        i.putExtra(getString(R.string.service_intent_message), m);
        Log.d(TAG, "Send Broadcast Message: " + i.getAction() + ":" + m);
        this.sendBroadcast(i);
    }
}
