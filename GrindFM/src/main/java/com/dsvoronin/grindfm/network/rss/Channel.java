package com.dsvoronin.grindfm.network.rss;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "channel", strict = false)
public class Channel {

    @ElementList(name = "item", inline = true)
    List<Article> articleList;

    @Element
    private String title;

    @Element
    private String link;

    @Element
    private String description;

    @Override
    public String toString() {
        return "Channel{" +
                "articleList=" + articleList +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}