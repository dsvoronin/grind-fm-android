package com.dsvoronin.grindfm.entities;

import android.support.annotation.Nullable;

public class NewsItem {

    private int itemId;

    private String title;

    private String description;

    private String pubDate;

    @Nullable
    private String imageUrl;

    private String link;

    public NewsItem(int itemId, String title, String description, String pubDate, @Nullable String imageUrl, String link) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.imageUrl = imageUrl;
        this.link = link;
    }

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPubDate() {
        return pubDate;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsItem newsItem = (NewsItem) o;

        if (itemId != newsItem.itemId) return false;
        if (!title.equals(newsItem.title)) return false;
        if (!description.equals(newsItem.description)) return false;
        if (!pubDate.equals(newsItem.pubDate)) return false;
        if (imageUrl != null ? !imageUrl.equals(newsItem.imageUrl) : newsItem.imageUrl != null)
            return false;
        return link.equals(newsItem.link);

    }

    @Override
    public int hashCode() {
        int result = itemId;
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + pubDate.hashCode();
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + link.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "itemId=" + itemId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
