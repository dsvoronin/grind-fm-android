package com.dsvoronin.grindfm.player;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.dsvoronin.grindfm.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class PlayerService extends MediaBrowserServiceCompat implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener {

    private static final String TAG = "PlayerService";

    private static final int NOTIFICATION_ID = 7112;

    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 10000;

    private static final float DUCK_VOLUME = 0.5f;

    private static final float FULL_VOLUME = 1.0f;

    private AudioManager audioManager;

    private MediaPlayer mediaPlayer;

    private MediaSessionCompat mediaSession;

    private boolean prepared = false;

    private final DelayedStopHandler delayedStopHandler = new DelayedStopHandler(this);

    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseRadio();
        }
    };

    private IntentFilter noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        ComponentName receiver = new ComponentName(getPackageName(), RemoteReceiver.class.getName());
        mediaSession = new MediaSessionCompat(this, TAG, receiver, null);

        setSessionToken(mediaSession.getSessionToken());

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Test Artist")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Test Album")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Test Track Name")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10000)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build());

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                startRadio();
            }

            @Override
            public void onPause() {
                pauseRadio();
            }
        });

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent);

        //todo remove receiver will crash here
        if (!mediaPlayer.isPlaying()) {
            startRadio();
        }

        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.d(TAG, "onDestroy");
        mediaPlayer.release();
        mediaSession.release();
        delayedStopHandler.removeCallbacksAndMessages(null);
        mediaSession.setCallback(null);
        mediaSession = null;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(
                getString(R.string.app_name),
                null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                pauseRadio();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseRadio();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                lowerVolume();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                startRadio();
                break;
        }
    }

    private void startRadio() {
        if (!prepared) {
            Uri parse = Uri.parse(getString(R.string.mp3_stream));
            try {
                mediaPlayer.setDataSource(this, parse);
            } catch (IOException e) {
                Log.e(TAG, "setDataSource error ", e);
            }
            mediaPlayer.prepareAsync();
        } else {
            actualStartPlayback();
        }
    }

    private void pauseRadio() {
        Log.d(TAG, "pauseRadio");

        mediaPlayer.pause();
        try {
            unregisterReceiver(noisyReceiver);
        } catch (Exception e) {
            //we don't care if register was not registered or we trying to unregister it second time
        }
        stopForeground(false);

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());

        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
    }

    private void lowerVolume() {
        mediaPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
        startRadio();
    }

    private void actualStartPlayback() {
        Log.d(TAG, "actualStartPlayback");

        mediaPlayer.setVolume(FULL_VOLUME, FULL_VOLUME);

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }

            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());

            delayedStopHandler.removeCallbacksAndMessages(null);

            startService(new Intent(getApplicationContext(), PlayerService.class));

            registerReceiver(noisyReceiver, noisyFilter);
            mediaPlayer.start();

            startForeground(NOTIFICATION_ID,
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_ic_launcher)
                            .setContentTitle("Title")
                            .setContentText("Text")
                            .setStyle(new NotificationCompat.MediaStyle()
                                    .setMediaSession(getSessionToken()))
                            .build());

        } else {
            Toast.makeText(this, getString(R.string.cant_get_audiofocus), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<PlayerService> mWeakReference;

        private DelayedStopHandler(PlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerService service = mWeakReference.get();
            if (service != null && service.mediaPlayer != null) {
                if (service.mediaPlayer.isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopForeground(true);
                service.stopSelf();
            }
        }
    }
}
