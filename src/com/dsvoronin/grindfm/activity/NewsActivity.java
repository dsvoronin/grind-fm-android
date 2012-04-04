package com.dsvoronin.grindfm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.task.NewsTask;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:17
 */
public class NewsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);

        String feed = getIntent().getStringExtra(getString(R.string.intent_news_feed));

        NewsAdapter adapter = new NewsAdapter(this);

        ListView newsList = (ListView) findViewById(R.id.news_list);
        newsList.setAdapter(adapter);
        newsList.setOnItemClickListener(onNewsClickListener);

        NewsTask task = new NewsTask(this, adapter);
        task.setProgress((ImageView) findViewById(R.id.news_progress));
        task.setTryAgain((Button) findViewById(R.id.news_try_again));

        if (feed == null) {
            task.execute(getString(R.string.rss_goha_grindfm));
        } else {
            task.execute(feed);
        }
    }

    private AdapterView.OnItemClickListener onNewsClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(NewsActivity.this, NewsDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra(getString(R.string.intent_news_detail), ((NewsAdapter) adapterView.getAdapter()).getItem(i));
            startActivity(intent);
        }
    };
}
