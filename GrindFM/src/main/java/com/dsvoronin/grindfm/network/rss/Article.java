package com.dsvoronin.grindfm.network.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "item", strict = false)
public class Article {

    @Element
    private String title;

    @Element
    private String description;

    @Element
    private String link;

    @Element(required = false)
    private String author;

    @Element(required = false)
    private String pubDate;

    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", link='" + link + '\'' +
                ", author='" + author + '\'' +
                ", pubDate='" + pubDate + '\'' +
                '}';
    }
}