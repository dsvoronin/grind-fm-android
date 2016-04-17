package com.dsvoronin.grindfm.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dsvoronin.grindfm.MainActivity;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.model.TrackListItem;
import com.dsvoronin.grindfm.network.GrindRequest;
import com.dsvoronin.grindfm.network.RequestManager;

import java.io.IOException;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "GrindFM.Player";

    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer player = null;

    private AudioManager audioManager;

    private WifiManager.WifiLock wifiLock;

    private TrackListItem lastItem = null;

    private UIUpdater updater = new UIUpdater(new Runnable() {

        @Override
        public void run() {
            RequestManager.getRequestQueue().add(new GrindRequest(getString(R.string.icecast_url), new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    String artistTitle = s.substring(s.indexOf(",,") + 2);
                    String[] split = artistTitle.split(" - ");
                    TrackListItem item = new TrackListItem(null, split[0], split[1], null);
                    if (!item.equals(lastItem)) {
                        startForeground(NOTIFICATION_ID, new Notification.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.cat_status_bar)
                                .setAutoCancel(false)
                                .setOngoing(true)
                                .setTicker(item.getArtist() + " - " + item.getTitle())
                                .setContentText(item.getArtist())
                                .setContentTitle(item.getTitle())
                                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                                        new Intent(getApplicationContext(), MainActivity.class),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                                .build());

                    }

                    lastItem = item;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e(TAG, "Can't update song", volleyError);
                }
            }
            ));
        }
    }, 10 * 1000);

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if (action == null) {
                    throw new IllegalArgumentException("Null action");
                }

                switch (Action.valueOf(action)) {
                    case REQUEST_STATUS:
                        sendStatusBroadcast(player != null && player.isPlaying());
                        break;
                    case FORCE_STOP:
                        stop();
                        player.reset();
                        break;
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unknown action: " + action, e);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        IntentFilter filter = new IntentFilter();
        for (Action action : Action.values()) {
            filter.addAction(action.name());
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (player == null || !player.isPlaying()) {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            player.setOnPreparedListener(this);
            player.setOnErrorListener(this);
            initPlayer();
        } else {
            player.stop();
            stop();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        play();
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        initPlayer();
        stop();

        return true;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);

        stop();

        if (player != null) {
            player.release();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
            Log.d(TAG, "Metadata update:" + extra);
        }
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "Focus gained");
                // resume playback
                if (player == null) {
                    initPlayer();
                } else if (!player.isPlaying()) {
                    play();
                    player.start();
                }
                player.setVolume(0.7f, 0.7f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "Focus lost");

                // Lost focus for an unbounded amount of time: stop playback and release media player
                stop();
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
                player = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "Focus lost for a short time");
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) {
                    stop();
                    player.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "Focus lost, can playback silently");
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private void initPlayer() {
        try {
            player.setDataSource(this, Uri.parse(getString(R.string.mp3_stream)));
            player.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Can't prepare data source", e);
        }
    }

    private void play() {

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Toast.makeText(this, getString(R.string.cant_get_audiofocus), Toast.LENGTH_SHORT).show();
            stop();
            return;
        }

        if (wifiLock != null && !wifiLock.isHeld()) {
            wifiLock.acquire();
        } else {
            throw new IllegalStateException("Wifi lock is null");
        }

        sendStatusBroadcast(true);

        updater.startUpdates();
    }

    private void stop() {
        lastItem = null;

        updater.stopUpdates();

        sendStatusBroadcast(false);

        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }

        stopForeground(true);
    }

    private void sendStatusBroadcast(boolean isPlaying) {
        Intent intent = new Intent(MainActivity.Action.PLAYER_STATUS_UPDATE.name());
        intent.putExtra("isPlaying", isPlaying);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public enum Action {
        REQUEST_STATUS, FORCE_STOP
    }
}
