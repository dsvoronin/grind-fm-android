package com.dsvoronin.grindfm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.task.BackgroundHttpTask;
import com.dsvoronin.grindfm.task.NewsTask;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:17
 */
public class NewsActivity extends HttpListActivity<NewsItem> {

    public static final String INTENT_EXTRA = "news-feed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.news;
    }

    @Override
    protected BaseListAdapter<NewsItem> createAdapter() {
        return new NewsAdapter(this, R.layout.news_item);
    }

    @Override
    protected ListView findListView() {
        return (ListView) findViewById(R.id.news_list);
    }

    @Override
    protected BackgroundHttpTask<NewsItem> createTask() {
        return new NewsTask(this);
    }

    @Override
    protected String getTaskParam() {
        String feed = getIntent().getStringExtra(INTENT_EXTRA);
        if (feed == null) {
            return getString(R.string.rss_goha_grindfm);
        } else {
            return feed;
        }
    }

    @Override
    protected AdapterView.OnItemClickListener defineItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(NewsActivity.this, NewsDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra(NewsDetailsActivity.INTENT_EXTRA, ((NewsAdapter) adapterView.getAdapter()).getItem(i));
                intent.putExtra("button-id", getIntent().getIntExtra("button-id", -1));
                startActivity(intent);
            }
        };
    }
}
