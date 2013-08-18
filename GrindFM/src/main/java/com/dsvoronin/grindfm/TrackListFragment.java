package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dsvoronin.grindfm.model.TrackListItem;
import com.dsvoronin.grindfm.network.GrindRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import static com.dsvoronin.grindfm.App.getApp;

public class TrackListFragment extends ListFragment implements PullToRefreshAttacher.OnRefreshListener {

    private static final String TAG = "GrindFM.TrackList";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private MainActivity activity;

    private PullToRefreshAttacher mPullToRefreshAttacher;

    private Handler handler;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        handler = new Handler();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPullToRefreshAttacher = activity.getPullToRefreshAttacher();
        mPullToRefreshAttacher.addRefreshableView(getListView(), this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "Item clicked: " + position);
//        activity.addFragment(new TrackInfoFragment());
        //todo
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onStart() {
        super.onStart();
        load();
    }

    @Override
    public void onDetach() {
        this.activity = null;
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        load();
    }

    private void load() {
        Log.d(TAG, "Loading...");
        GrindRequest request = new GrindRequest(getString(R.string.tracklist_url), new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                Log.d(TAG, "Load complete... " + string);
                try {
                    String hacked = string.substring(string.indexOf('['), string.indexOf(']') + 1);
                    TrackListItem[] trackList = mapper.readValue(hacked, TrackListItem[].class);

                    if (trackList == null || trackList.length == 0) {
                        return;
                    }

                    scheduleUpdate(trackList[0]);

                    ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
                    for (TrackListItem item : trackList) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("timestamp", parseDate(item.getDate()));
                        map.put("artist", item.getArtist());
                        map.put("title", item.getTitle());
                        data.add(map);
                    }
                    setListAdapter(new SimpleAdapter(activity, data, R.layout.tracklist_item, new String[]{"timestamp", "artist", "title"}, new int[]{R.id.track_timestamp, R.id.track_artist, R.id.track_title}));

                    mPullToRefreshAttacher.setRefreshComplete();
                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Volley error", volleyError);
//                if (activity != null) {
//                    Toast.makeText(activity, getString(R.string.default_server_error), Toast.LENGTH_SHORT).show();
//                }

                mPullToRefreshAttacher.setRefreshComplete();
            }
        }
        );
        getApp().getQueue().add(request);
    }

    private void scheduleUpdate(TrackListItem newestItem) {
        String date = newestItem.getDate();
        String substring = date.substring(0, date.indexOf('.'));
        long newestSongStartTime = Long.parseLong(substring) * 1000;

        String duration = newestItem.getDuration();
        long millis;
        if (duration == null || duration.trim().equals("")) {
            millis = 180000;
        } else {
            millis = Long.parseLong(duration) * 1000;
        }

        long time = newestSongStartTime + millis - System.currentTimeMillis();
        if (time < 0) {
            time = 180000;
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity != null) {
                    load();
                }
            }
        }, time);
    }

    private String parseDate(String date) {
        String substring = date.substring(0, date.indexOf('.'));
        long l = Long.parseLong(substring) * 1000;
        Date date1 = new Date(l);
        return DATE_FORMAT.format(date1);
    }
}
