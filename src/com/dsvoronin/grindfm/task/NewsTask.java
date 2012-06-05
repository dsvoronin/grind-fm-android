package com.dsvoronin.grindfm.task;

import android.util.Log;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.rss.FeedParser;
import com.dsvoronin.grindfm.rss.SaxFeedParser;

import java.util.ArrayList;
import java.util.Collections;

public class NewsTask extends BackgroundHttpTask<NewsItem> {

    private static final String TAG = "Grind.NewsTask";

    public NewsTask(HttpListActivity<NewsItem> newsItemHttpListActivity) {
        super(newsItemHttpListActivity);
    }

    @Override
    protected ArrayList<NewsItem> processAsync(String... urls) {
        ArrayList<NewsItem> result = new ArrayList<NewsItem>();

        for (String url : urls) {
            try {
                FeedParser parser = new SaxFeedParser(url);
                result.addAll(parser.parse());
            } catch (Exception e) {
                Log.e(TAG, "error", e);
            }
        }

        Collections.sort(result);
        return result;
    }
}
