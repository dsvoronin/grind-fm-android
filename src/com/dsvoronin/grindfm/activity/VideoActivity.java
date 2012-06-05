package com.dsvoronin.grindfm.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.adapter.VideoAdapter;
import com.dsvoronin.grindfm.model.Video;
import com.dsvoronin.grindfm.task.BackgroundHttpTask;
import com.dsvoronin.grindfm.task.VideoTask;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 19:24
 */
public class VideoActivity extends HttpListActivity<Video> {

    private static final String TAG = "Grind.VideoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.video;
    }

    @Override
    protected BaseListAdapter<Video> createAdapter() {
        return new VideoAdapter(this, R.layout.video_item);
    }

    @Override
    protected ListView findListView() {
        return (ListView) findViewById(R.id.video_list);
    }

    @Override
    protected BackgroundHttpTask<Video> createTask() {
        return new VideoTask(this);
    }

    @Override
    protected String getTaskParam() {
        return null;
    }

    @Override
    protected AdapterView.OnItemClickListener defineItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String requestURL = getString(R.string.youtube_watch) + ((VideoAdapter) adapterView.getAdapter()).getItem(i).getUrl();
                Log.d(TAG, "requestURL = " + requestURL);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestURL)));
            }
        };
    }
}
