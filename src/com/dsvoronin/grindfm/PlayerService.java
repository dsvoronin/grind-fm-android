package com.dsvoronin.grindfm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

/**
 * User: dsvoronin
 * Date: 15.06.12
 * Time: 5:38
 * <p/>
 * Сервис для проигрывания аудио стрима grind.fm
 * <p/>
 * todo list:
 * - wifilock? 3glock?(существует ли?)
 * - audio become noisy (headphones disconnect)
 */
public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "Grind.PlayerService";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer player;

    private AudioManager audioManager;


    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(getString(R.string.action_play))) {
            if (player != null) {
                if (player.isPlaying()) {
                    pausePlayer(true);
                } else {
                    startPlayer();
                }
            } else {
                startPlayer();
            }
        } else if (intent.getAction().equals(getString(R.string.action_request))) {
            if (player != null && player.isPlaying()) {
                sendDisplayAction(getResources().getInteger(R.integer.display_playing));
            } else {
                sendDisplayAction(getResources().getInteger(R.integer.display_paused));
            }
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "Player prepared");
        startPlayer();
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        String whatString;
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                whatString = "unknown error";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                whatString = "server died";
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                whatString = "not valid for progressive playback";
                break;
            default:
                whatString = "unknown error code";
        }
        Log.d(TAG, "Playback error: " + whatString + extra);

        stopPlayer(true);
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "Playback complete");
        stopPlayer(true);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                startPlayer();
                player.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                stopPlayer(false);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                pausePlayer(false);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            audioManager.abandonAudioFocus(this);
            sendDisplayAction(getResources().getInteger(R.integer.display_paused));
        }
    }

    private MediaPlayer initMediaPlayer() {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_mp3)));
            player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            player.setOnPreparedListener(this);
            player.setOnErrorListener(this);
            return player;
        } catch (IOException e) {
            Log.e(TAG, "Invalid datasource", e);
            return null;
        }
    }

    private void startPlayer() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //todo message UI
            Log.e(TAG, "Can't get audio focus");
            return;
        }

        if (player == null) {
            player = initMediaPlayer();
            player.prepareAsync();
            sendDisplayAction(getResources().getInteger(R.integer.display_progress));
        } else if (!player.isPlaying()) {
            player.start();
            sendDisplayAction(getResources().getInteger(R.integer.display_playing));
        }
        startForeground(NOTIFICATION_ID, new PlayerNotification(this, "Grind.FM Song").buildNotification());

        Log.d(TAG, "Player started");
    }

    private void pausePlayer(boolean abandonFocus) {
        if (player != null) {
            player.pause();
        }
        sendDisplayAction(getResources().getInteger(R.integer.display_paused));
        stopForeground(true);

        if (abandonFocus) {
            audioManager.abandonAudioFocus(this);
        }

        Log.d(TAG, "Player paused");
    }

    private void stopPlayer(boolean abandonFocus) {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        sendDisplayAction(getResources().getInteger(R.integer.display_paused));
        stopForeground(true);

        if (abandonFocus) {
            audioManager.abandonAudioFocus(this);
        }

        Log.d(TAG, "Player stopped");
    }

    private void sendDisplayAction(int what) {
        Log.d(TAG, "sending display action " + what);
        Intent intent = new Intent(getString(R.string.action_display));
        intent.putExtra(getString(R.string.action_display), what);
        sendBroadcast(intent);
    }
}
