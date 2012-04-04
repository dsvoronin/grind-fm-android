package com.dsvoronin.grindfm.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.VideoAdapter;
import com.dsvoronin.grindfm.task.VideoTask;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 19:24
 */
public class VideoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);

        VideoAdapter adapter = new VideoAdapter(this);

        ListView videoList = (ListView) findViewById(R.id.video_list);
        videoList.setAdapter(adapter);
        videoList.setOnItemClickListener(onVideoClickListener);

        VideoTask task = new VideoTask(this, adapter);
        task.setProgress((ImageView) findViewById(R.id.video_progress));
        task.setTryAgain((Button) findViewById(R.id.video_try_again));
        //noinspection unchecked
        task.execute();
    }

    private AdapterView.OnItemClickListener onVideoClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "http://www.youtube.com/watch?v=" + ((VideoAdapter) adapterView.getAdapter()).getItem(i).getUrl()
            )));
        }
    };
}
