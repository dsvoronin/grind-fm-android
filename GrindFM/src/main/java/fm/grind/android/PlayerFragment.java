package fm.grind.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaMetadataCompat;
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
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fm.grind.android.entities.TrackInList;
import fm.grind.android.sync.TracksHistoryLoader;

public class PlayerFragment extends Fragment {

    private static final String TAG = "PlayerFragment";

    private static final int LAST_PLAYED_TRACKS_LOADER_ID = 1;

    private static final long LAST_PLAYED_TRACKS_POLL_FREQUENCY = TimeUnit.SECONDS.toMillis(60);

    private ImageView playPauseButton;

    private TextView trackName;

    private TextView trackArtist;

    private RecyclerView tracksHistoryView;

    private TrackListAdapter adapter;

    private Tracker tracker;

    private Handler handler = new Handler();

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            Log.d(TAG, "Received playback state change to state " + state.getState());
            PlayerFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            Log.d(TAG, "Received metadata state change to mediaId=" +
                    metadata.getDescription().getMediaId() +
                    " song=" + metadata.getDescription().getTitle());
            PlayerFragment.this.onMetadataChanged(metadata);
        }
    };

    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaControllerCompat controller = getActivity().getSupportMediaController();
            PlaybackStateCompat stateObj = controller.getPlaybackState();
            final int state = stateObj == null ?
                    PlaybackStateCompat.STATE_NONE : stateObj.getState();
            Log.d(TAG, "Button pressed, in state " + state);
            switch (v.getId()) {
                case R.id.play_pause:
                    Log.d(TAG, "Play button pressed, in state " + state);

                    if (tracker != null) {
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Play button")
                                .build());
                    }

                    if (state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED ||
                            state == PlaybackStateCompat.STATE_NONE) {
                        playMedia();
                    } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                            state == PlaybackStateCompat.STATE_BUFFERING ||
                            state == PlaybackStateCompat.STATE_CONNECTING) {
                        pauseMedia();
                    }
                    break;
            }
        }
    };

    private void playMedia() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }

    private Runnable pollLastPlayedTracks = new Runnable() {
        @Override
        public void run() {
            getLoaderManager().getLoader(LAST_PLAYED_TRACKS_LOADER_ID).forceLoad();
            handler.postDelayed(this, LAST_PLAYED_TRACKS_POLL_FREQUENCY);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        tracker = App.fromContext(context).getDefaultTracker();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");

        Context context = getContext();

        adapter = new TrackListAdapter(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        playPauseButton = (ImageView) view.findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(mButtonListener);

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
        getLoaderManager().initLoader(LAST_PLAYED_TRACKS_LOADER_ID, null, lastPlayedTracksLoaderCallbacks);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "fragment.onStart");
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "fragment.onStop");
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(mCallback);
        }
    }

    public void onConnected() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        Log.d(TAG, "onConnected, mediaController==null? " + (controller == null));
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged " + state);
        if (getActivity() == null) {
            Log.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
            case PlaybackStateCompat.STATE_NONE:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Log.e(TAG, "error playbackstate: " + state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        if (enablePlay) {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_black_24dp));
        } else {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_black_24dp));
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d(TAG, "onMetadataChanged " + metadata);
        if (getActivity() == null) {
            Log.w(TAG, "onMetadataChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (metadata == null) {
            return;
        }

        trackName.setText(metadata.getDescription().getTitle());
        trackArtist.setText(metadata.getDescription().getSubtitle());
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(pollLastPlayedTracks);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollLastPlayedTracks);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(LAST_PLAYED_TRACKS_LOADER_ID);
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