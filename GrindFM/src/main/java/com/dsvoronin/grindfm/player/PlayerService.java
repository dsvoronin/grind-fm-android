package com.dsvoronin.grindfm.player;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.dsvoronin.grindfm.App;
import com.dsvoronin.grindfm.MainActivity;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.entities.CurrentTrack;
import com.dsvoronin.grindfm.sync.GrindService;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerService extends MediaBrowserServiceCompat implements
        PlaybackManager.PlaybackServiceCallback {

    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.example.android.uamp.ACTION_CMD";

    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";

    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";

    private static final String TAG = "PlayerService";

    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 10000;

    private static final long CURRENT_TRACK_POLL_FREQUENCY = TimeUnit.SECONDS.toMillis(30);

    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mediaSession;

    private MusicNotificationManager musicNotificationManager;

    private Handler handler = new Handler();

    private final DelayedStopHandler delayedStopHandler = new DelayedStopHandler(this);

    private Runnable pollCurrentTrack = new Runnable() {
        @Override
        public void run() {
            new CurrentTrackAsyncTask(PlayerService.this).execute();
            handler.postDelayed(this, CURRENT_TRACK_POLL_FREQUENCY);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        LocalPlayback playback = new LocalPlayback(this, getString(R.string.ogg_stream));
        mPlaybackManager = new PlaybackManager(this, getResources(), playback);

        // Start a new MediaSession
        mediaSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mediaSession.getSessionToken());
        mediaSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pi);

        mPlaybackManager.updatePlaybackState(null);

        try {
            musicNotificationManager = new MusicNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }

        handler.post(pollCurrentTrack);
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mediaSession, startIntent);
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        handler.removeCallbacks(pollCurrentTrack);
        // Service is being killed, so make sure we release our resources
        mPlaybackManager.handleStopRequest(null);
        musicNotificationManager.stopNotification();
        delayedStopHandler.removeCallbacksAndMessages(null);
        mediaSession.release();
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

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }

        delayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), PlayerService.class));
    }


    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        musicNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mediaSession.setPlaybackState(newState);
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
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

    private class CurrentTrackAsyncTask extends AsyncTask<Void, Void, CurrentTrack> {

        private static final String TAG = "CurrentTrackLoader";

        private GrindService grindService;

        public CurrentTrackAsyncTask(Context context) {
            grindService = App.fromContext(context).getGrindService();
        }

        @Override
        protected CurrentTrack doInBackground(Void... params) {
            try {
                return grindService.getCurrentSong().execute().body();
            } catch (IOException e) {
                Log.e(TAG, "Can't load current song", e);
                return CurrentTrack.NULL;
            }
        }

        @Override
        protected void onPostExecute(CurrentTrack currentTrack) {
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.getTrack())
                    .build());
        }
    }
}
