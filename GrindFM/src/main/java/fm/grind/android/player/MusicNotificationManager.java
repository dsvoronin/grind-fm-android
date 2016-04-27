package fm.grind.android.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import fm.grind.android.MainActivity;
import fm.grind.android.R;
import fm.grind.android.utils.ResourceHelper;

public class MusicNotificationManager extends BroadcastReceiver {

    private static final String TAG = "MusicNotification";

    private static final int NOTIFICATION_ID = 411;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "fm.grind.pause";
    public static final String ACTION_PLAY = "fm.grind.play";

    private final PlayerService service;

    private final NotificationManagerCompat notificationManager;

    private MediaControllerCompat controller;
    private MediaControllerCompat.TransportControls transportControls;

    private MediaSessionCompat.Token sessionToken;

    private final PendingIntent pauseIntent;
    private final PendingIntent playIntent;

    private PlaybackStateCompat playbackState;
    private MediaMetadataCompat metadata;

    private final int notificationColor;

    private boolean started = false;

    public MusicNotificationManager(PlayerService service) throws RemoteException {
        this.service = service;
        updateSessionToken();

        notificationManager = NotificationManagerCompat.from(service);

        notificationColor = ResourceHelper.getThemeColor(service, R.attr.colorPrimary,
                Color.DKGRAY);

        String pkg = service.getPackageName();

        pauseIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        playIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        notificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
        if (!started) {
            metadata = controller.getMetadata();
            playbackState = controller.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                controller.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                service.registerReceiver(this, filter);

                service.startForeground(NOTIFICATION_ID, notification);
                started = true;
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (started) {
            started = false;
            controller.unregisterCallback(mCb);
            try {
                notificationManager.cancel(NOTIFICATION_ID);
                service.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            service.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                transportControls.pause();
                break;
            case ACTION_PLAY:
                transportControls.play();
                break;
            default:
                Log.w(TAG, "Unknown intent ignored. Action=" + action);
        }
    }

    private void addPlayPauseAction(NotificationCompat.Builder builder) {
        String label;
        int icon;
        PendingIntent intent;
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = service.getString(R.string.action_player_pause);
            icon = R.drawable.ic_pause_black_24dp;
            intent = pauseIntent;
        } else {
            label = service.getString(R.string.action_player_play);
            icon = R.drawable.ic_play_arrow_black_24dp;
            intent = playIntent;
        }
        builder.addAction(new NotificationCompat.Action(icon, label, intent));
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = service.getSessionToken();
        if (sessionToken == null && freshToken != null ||
                sessionToken != null && !sessionToken.equals(freshToken)) {
            if (controller != null) {
                controller.unregisterCallback(mCb);
            }
            sessionToken = freshToken;
            if (sessionToken != null) {
                controller = new MediaControllerCompat(service, sessionToken);
                transportControls = controller.getTransportControls();
                if (started) {
                    controller.registerCallback(mCb);
                }
            }
        }
    }

    private Notification createNotification() {
        Log.d(TAG, "updateNotificationMetadata. mMetadata=" + metadata);
        if (metadata == null || playbackState == null) {
            return null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service);
        int playPauseButtonPosition = 0;

        addPlayPauseAction(notificationBuilder);

        MediaDescriptionCompat description = metadata.getDescription();

        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{playPauseButtonPosition})  // show only play/pause in compact view
                        .setMediaSession(sessionToken))
                .setColor(notificationColor)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(false)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle());

        setNotificationPlaybackState(notificationBuilder);

        return notificationBuilder.build();
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(service, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(service, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        Log.d(TAG, "updateNotificationPlaybackState. mPlaybackState=" + playbackState);
        if (playbackState == null || !started) {
            Log.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
            service.stopForeground(true);
            return;
        }
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && playbackState.getPosition() >= 0) {
            Log.d(TAG, "updateNotificationPlaybackState. updating playback position to " +
                    (System.currentTimeMillis() - playbackState.getPosition()) / 1000 + " seconds");
            builder
                    .setWhen(System.currentTimeMillis() - playbackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            Log.d(TAG, "updateNotificationPlaybackState. hiding playback position");
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            playbackState = state;
            Log.d(TAG, "Received new playback state: " + state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    notificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat meta) {
            metadata = meta;
            Log.d(TAG, "Received new metadata: " + metadata);
            Notification notification = createNotification();
            if (notification != null) {
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            Log.d(TAG, "Session was destroyed, resetting to the new session token");
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                Log.e(TAG, "could not connect media controller", e);
            }
        }
    };
}
