package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.RequestSongAdapter;
import com.dsvoronin.grindfm.task.RequestSearchTask;
import com.dsvoronin.grindfm.task.RequestTask;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 23:08
 */
public class RequestActivity extends BaseActivity {

    private RequestSongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request);

        String requestText = getIntent().getStringExtra(getString(R.string.intent_request_string));

        adapter = new RequestSongAdapter(this);

        ListView searchList = (ListView) findViewById(R.id.request_list);
        searchList.setAdapter(adapter);
        searchList.setOnItemClickListener(onSongClickListener);

        RequestSearchTask task = new RequestSearchTask(this, adapter);
        task.setProgress((ImageView) findViewById(R.id.request_progress));
        task.setTryAgain((Button) findViewById(R.id.request_try_again));
        task.execute(requestText);
    }

    private AdapterView.OnItemClickListener onSongClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            RequestTask task = new RequestTask(RequestActivity.this);
            task.execute(adapter.getItem(i).getId());
        }
    };
}
