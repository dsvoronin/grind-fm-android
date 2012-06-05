package com.dsvoronin.grindfm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.dsvoronin.grindfm.activity.MainActivity;
import com.dsvoronin.grindfm.util.GrindHttpClient;
import com.dsvoronin.grindfm.util.GrindHttpClientException;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GrindService extends Service {

    private final String TAG = "Grind.Service";

    private static final int NOTIFICATION_ID = 6;

    public static final int COMMAND_GET_STATUS = 4;

    private MediaPlayer player;

    private boolean playing = false;

    private NotificationManager notificationManager;

    private PlayerHandler handler;

    private PlayerReceiver receiver;

    private class PlayerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COMMAND_GET_STATUS:
                    if (playing) {
                        sendCommand(ServiceHandler.COMMAND_START);
                        GrindService.this.sendMessage(getInfo());
                    } else {
                        sendCommand(ServiceHandler.COMMAND_STOP);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (handler == null) {
                handler = new PlayerHandler();
            }

            int command = intent.getIntExtra("player-command", -1);
            if (command != -1) {
                handler.sendEmptyMessage(command);
            } else {
                Log.d(TAG, "Incorrect command: -1");
            }
        }
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (playing) {
                        Log.d(TAG, "Someone's calling. Stop playback");
                        player.stop();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (!playing) {
                        Log.d(TAG, "Call state - idle. Restart playback");
                        player.start();
                    }
                    break;
                default:
                    Log.d(TAG, "Unknown phone state = " + state);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(TAG, "Created");

        receiver = new PlayerReceiver();
        registerReceiver(receiver, new IntentFilter("player-intent"));

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable() {
            private String info;

            @Override
            public void run() {
                String newInfo = getInfo();
                if (info == null) {
                    info = newInfo;
                } else {
                    if (!info.equals(newInfo)) {
                        sendMessage(newInfo);
                        showNotification(newInfo);
                        info = newInfo;
                    }
                }
            }
        }, 20, 20, TimeUnit.SECONDS);
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
        stop();
        unregisterReceiver(receiver);
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
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_ogg)));
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Media prepared");

                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

                    player.start();
                    playing = true;
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
                    playing = false;
                    return false;
                }
            });
            player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int extras) {
                    Log.d(TAG, what + " " + extras);
                    if (what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
                        Log.d(TAG, "Metadata updated " + extras);
                    }
                    return false;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Complete!");
                }
            });

            player.prepareAsync();

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
        GrindHttpClient httpClient = new GrindHttpClient(getResources().getInteger(R.integer.connection_timeout), getResources().getInteger(R.integer.socket_timeout));
        try {
            String response = httpClient.request(new HttpGet(getString(R.string.radio_stream_meta)));
            return response.substring(response.indexOf(",,") + 2);
        } catch (GrindHttpClientException e) {
            Log.e(TAG, "Error while parsing icecast", e);
            return "Нет данных";
        }
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
