package com.dsvoronin.grindfm.task;

import android.content.Context;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.rss.SaxFeedParser;

import java.util.ArrayList;

public class RssParseTask extends BaseTask {

    public RssParseTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
    }

    @Override
    protected ArrayList processAsync(String url) throws Exception {
        SaxFeedParser parser = new SaxFeedParser(url);
        return new ArrayList<NewsItem>(parser.parse());
    }

    @Override
    protected void afterTaskActions() {
    }
}
