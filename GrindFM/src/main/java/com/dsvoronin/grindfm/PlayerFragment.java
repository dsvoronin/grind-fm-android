package com.dsvoronin.grindfm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsvoronin.grindfm.entities.CurrentTrack;
import com.dsvoronin.grindfm.entities.TrackInList;
import com.dsvoronin.grindfm.player.PlayerService;
import com.dsvoronin.grindfm.sync.CurrentTrackLoader;
import com.dsvoronin.grindfm.sync.TracksHistoryLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";

    private static final int CURRENT_TRACK_LOADER_ID = 0;
    private static final int LAST_PLAYED_TRACKS_LOADER_ID = 1;

    private static final long CURRENT_TRACK_POLL_FREQUENCY = TimeUnit.SECONDS.toMillis(30);
    private static final long LAST_PLAYED_TRACKS_POLL_FREQUENCY = TimeUnit.SECONDS.toMillis(60);

    private ImageView button;

    private TextView trackName;

    private TextView trackArtist;

    private RecyclerView tracksHistoryView;

    private TrackListAdapter adapter;

    private MediaBrowserCompat mediaBrowser;

    private MediaControllerCompat mediaController;

    private Handler handler = new Handler();

    private Runnable pollCurrentTrack = new Runnable() {
        @Override
        public void run() {
            getLoaderManager().getLoader(CURRENT_TRACK_LOADER_ID).forceLoad();
            handler.postDelayed(this, CURRENT_TRACK_POLL_FREQUENCY);
        }
    };

    private Runnable pollLastPlayedTracks = new Runnable() {
        @Override
        public void run() {
            getLoaderManager().getLoader(LAST_PLAYED_TRACKS_LOADER_ID).forceLoad();
            handler.postDelayed(this, LAST_PLAYED_TRACKS_POLL_FREQUENCY);
        }
    };

    private LoaderManager.LoaderCallbacks<CurrentTrack> currentTrackLoaderCallbacks = new LoaderManager.LoaderCallbacks<CurrentTrack>() {
        @Override
        public Loader<CurrentTrack> onCreateLoader(int id, Bundle args) {
            return new CurrentTrackLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<CurrentTrack> loader, CurrentTrack data) {
            trackName.setText(data.getTrack());
            trackArtist.setText(data.getArtist());
        }

        @Override
        public void onLoaderReset(Loader<CurrentTrack> loader) {
            CurrentTrack data = CurrentTrack.NULL;
            trackName.setText(data.getTrack());
            trackArtist.setText(data.getArtist());
        }
    };

    private LoaderManager.LoaderCallbacks<List<TrackInList>> lastPlayedTracksLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<TrackInList>>() {
        @Override
        public Loader<List<TrackInList>> onCreateLoader(int id, Bundle args) {
            return new TracksHistoryLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<TrackInList>> loader, List<TrackInList> data) {
            adapter.update(data);
        }

        @Override
        public void onLoaderReset(Loader<List<TrackInList>> loader) {
            adapter.update(Collections.<TrackInList>emptyList());
        }
    };

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

        adapter = new TrackListAdapter(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        button = (ImageView) view.findViewById(R.id.play_pause);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(new Intent(getActivity(), PlayerService.class));
            }
        });

        trackName = (TextView) view.findViewById(R.id.track_name);
        trackArtist = (TextView) view.findViewById(R.id.track_artist);

        tracksHistoryView = (RecyclerView) view.findViewById(R.id.track_history);
        tracksHistoryView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tracksHistoryView.setAdapter(adapter);
        getLoaderManager().initLoader(CURRENT_TRACK_LOADER_ID, null, currentTrackLoaderCallbacks);
        getLoaderManager().initLoader(LAST_PLAYED_TRACKS_LOADER_ID, null, lastPlayedTracksLoaderCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(pollCurrentTrack);
        handler.post(pollLastPlayedTracks);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollCurrentTrack);
        handler.removeCallbacks(pollLastPlayedTracks);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(CURRENT_TRACK_LOADER_ID);
        getLoaderManager().destroyLoader(LAST_PLAYED_TRACKS_LOADER_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mediaBrowser.disconnect();
    }

    private static class TrackListAdapter extends RecyclerView.Adapter<TrackListItemViewHolder> {

        private LayoutInflater inflater;

        private List<TrackInList> data = new ArrayList<>();

        public TrackListAdapter(Context context) {
            setHasStableIds(true);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public TrackListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TrackListItemViewHolder(inflater.inflate(R.layout.track_item, parent, false));
        }

        @Override
        public void onBindViewHolder(TrackListItemViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void update(List<TrackInList> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }
    }

    private static class TrackListItemViewHolder extends RecyclerView.ViewHolder {

        private TextView trackView;

        private TextView timeView;

        public TrackListItemViewHolder(View itemView) {
            super(itemView);
            trackView = (TextView) itemView.findViewById(R.id.track_name);
            timeView = (TextView) itemView.findViewById(R.id.played_at);
        }

        public void bind(TrackInList trackInList) {
            trackView.setText(trackInList.getTrack());
            timeView.setText(trackInList.getDate());
        }
    }
}

