package com.dsvoronin.grindfm.task;

import android.content.Context;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.rss.SaxFeedParser;

import java.util.ArrayList;
import java.util.Collections;

public class NewsTask extends BaseTask {

    public NewsTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
    }

    @Override
    protected ArrayList processAsync(String... urls) throws Exception {
        ArrayList<NewsItem> result = new ArrayList<NewsItem>();

        for (String url : urls) {
            SaxFeedParser parser = new SaxFeedParser(url);
            result.addAll(parser.parse());
        }

        Collections.sort(result);

        return result;
    }

    @Override
    protected void afterTaskActions() {
    }
}
