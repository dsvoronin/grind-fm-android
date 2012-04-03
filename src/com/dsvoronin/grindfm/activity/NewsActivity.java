package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.task.RssParseTask;

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

        NewsAdapter adapter = new NewsAdapter(this);

        ListView newsList = (ListView) findViewById(R.id.news_list);
        newsList.setAdapter(adapter);

        RssParseTask task = new RssParseTask(this, adapter);
        task.setProgress((ImageView) findViewById(R.id.news_progress));
        task.setTryAgain((Button) findViewById(R.id.news_try_again));
        task.execute(getString(R.string.rss_goha_grindfm));
    }
}
