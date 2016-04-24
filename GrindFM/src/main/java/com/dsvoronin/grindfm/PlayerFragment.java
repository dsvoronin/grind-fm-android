package com.dsvoronin.grindfm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsvoronin.grindfm.entities.Track;
import com.dsvoronin.grindfm.player.PlayerService;
import com.dsvoronin.grindfm.sync.CurrentTrackLoader;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_CONNECTING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_ERROR;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_FAST_FORWARDING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_REWINDING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED;

public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Track> {

    private static final String TAG = "PlayerFragment";

    private static final long TICK = TimeUnit.SECONDS.toMillis(30);

    private ImageButton button;

    private TextView trackName;

    private TextView trackArtist;

    private MediaBrowserCompat mediaBrowser;

    private MediaControllerCompat mediaController;

    private Timer timer;

    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d(TAG, "Playback state changed: " + state.toString());
            switch (state.getState()) {
                case STATE_BUFFERING:
                case STATE_CONNECTING:
                case STATE_PLAYING:
                    button.setImageResource(R.drawable.ic_pause_black_24dp);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mediaController.getTransportControls().pause();
                        }
                    });
                    break;
                case STATE_NONE:
                case STATE_PAUSED:
                case STATE_STOPPED:
                    button.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().startService(new Intent(getActivity(), PlayerService.class));
                        }
                    });
                    break;
                case STATE_ERROR:
                case STATE_FAST_FORWARDING:
                case STATE_REWINDING:
                case STATE_SKIPPING_TO_NEXT:
                case STATE_SKIPPING_TO_PREVIOUS:
                case STATE_SKIPPING_TO_QUEUE_ITEM:
                    //todo handle these
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");

        Context context = getContext();

        mediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(context, PlayerService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        Log.d(TAG, "MediaBrowserCompat.onConnected");
                        try {
                            mediaController = new MediaControllerCompat(getActivity(), mediaBrowser.getSessionToken());
                            mediaController.registerCallback(controllerCallback);
                            controllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
                        } catch (RemoteException e) {
                            Log.e(TAG, "MediaBrowserCompat.could not connect media controller", e);
                            button.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.d(TAG, "MediaBrowserCompat.onConnectionFailed");
                    }

                    @Override
                    public void onConnectionSuspended() {
                        Log.d(TAG, "MediaBrowserCompat.onConnectionSuspended");
                        mediaController.unregisterCallback(controllerCallback);
                    }
                }, null);

        mediaBrowser.connect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        button = (ImageButton) view.findViewById(R.id.play_pause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(), PlayerService.class));
            }
        });

        trackName = (TextView) view.findViewById(R.id.track_name);
        trackArtist = (TextView) view.findViewById(R.id.track_artist);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getLoaderManager().getLoader(0).forceLoad();
            }
        }, 0, TICK);
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mediaBrowser.disconnect();
    }

    @Override
    public Loader<Track> onCreateLoader(int id, Bundle args) {
        return new CurrentTrackLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Track> loader, Track track) {
        trackName.setText(track.getTrack());
        trackArtist.setText(track.getArtist());
    }

    @Override
    public void onLoaderReset(Loader<Track> loader) {
        trackName.setText("");
        trackArtist.setText("");
    }
}

