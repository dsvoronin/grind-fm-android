package com.dsvoronin.grindfm.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseFeedParser implements FeedParser {

    // names of the XML tags
    static final String CHANNEL = "channel";
    static final String PUB_DATE = "pubDate";
    static final String DESCRIPTION = "description";
    static final String LINK = "link";
    static final String TITLE = "title";
    static final String ITEM = "item";

    private final URL feedUrl;

    protected BaseFeedParser(String feedUrl) throws MalformedURLException {
        this.feedUrl = new URL(feedUrl);
    }

    protected InputStream getInputStream() {
        try {
            return feedUrl.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}