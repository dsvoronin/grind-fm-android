package com.dsvoronin.grindfm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dsvoronin.grindfm.network.GrindRequest;
import com.dsvoronin.grindfm.network.RequestManager;
import com.dsvoronin.grindfm.rss.RssFeed;
import com.dsvoronin.grindfm.rss.RssReader;

import org.jetbrains.annotations.Nullable;

public class NewsFragment extends Fragment {

    private static final String TAG = "GrindFM.News";

    private NewsAdapter adapter;

    private AbsListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new NewsAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            view = inflater.inflate(R.layout.news, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (AbsListView) view.findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        load();
    }

    private void load() {
        GrindRequest request = new GrindRequest(getString(R.string.rss_url), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    RssFeed read = RssReader.read(s);
                    adapter.setContent(read.getRssItems());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing news rss", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Error loading news rss", volleyError);
            }
        }
        );
        RequestManager.getRequestQueue().add(request);
    }
}
