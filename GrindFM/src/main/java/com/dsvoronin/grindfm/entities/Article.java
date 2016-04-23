package com.dsvoronin.grindfm.entities;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "item", strict = false)
public class Article {

    @Element
    public String title;

    @Element
    public String description;

    @Element
    public String link;

    @Element(name = "dc:creator", required = false)
    public String author;

    @Element(required = false)
    public String pubDate;

    @Element
    public String guid;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getAuthor() {
        return author;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", link='" + link + '\'' +
                ", author='" + author + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", guid='" + guid + '\'' +
                '}';
    }
}