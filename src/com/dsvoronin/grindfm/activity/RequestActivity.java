package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.adapter.RequestSongAdapter;
import com.dsvoronin.grindfm.model.RequestSong;
import com.dsvoronin.grindfm.task.BackgroundHttpTask;
import com.dsvoronin.grindfm.task.RequestSearchTask;
import com.dsvoronin.grindfm.task.RequestTask;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 23:08
 */
public class RequestActivity extends HttpListActivity<RequestSong> {

    public static final String INTENT_EXTRA = "request-string";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.request;
    }

    @Override
    protected BaseListAdapter<RequestSong> createAdapter() {
        return new RequestSongAdapter(this, R.layout.request_item);
    }

    @Override
    protected ListView findListView() {
        return (ListView) findViewById(R.id.request_list);
    }

    @Override
    protected BackgroundHttpTask<RequestSong> createTask() {
        return new RequestSearchTask(this);
    }

    @Override
    protected String getTaskParam() {
        return getIntent().getStringExtra(INTENT_EXTRA);
    }

    @Override
    protected AdapterView.OnItemClickListener defineItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RequestTask task = new RequestTask(RequestActivity.this);
                task.execute(((RequestSongAdapter) adapterView.getAdapter()).getItem(i).getId());
            }
        };
    }
}
