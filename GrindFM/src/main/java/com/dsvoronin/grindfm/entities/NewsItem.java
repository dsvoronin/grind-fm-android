package com.dsvoronin.grindfm.entities;

import android.support.annotation.Nullable;

public class NewsItem {

    private final int itemId;

    private final String title;

    private final String description;

    private final long pubDate;

    @Nullable
    private final String imageUrl;

    private final String link;

    private final String formattedDate;

    public NewsItem(int itemId, String title, String description, long pubDate, @Nullable String imageUrl, String link, String formattedDate) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.imageUrl = imageUrl;
        this.link = link;
        this.formattedDate = formattedDate;
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

    public long getPubDate() {
        return pubDate;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public String getLink() {
        return link;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewsItem newsItem = (NewsItem) o;

        if (itemId != newsItem.itemId) return false;
        if (pubDate != newsItem.pubDate) return false;
        if (!title.equals(newsItem.title)) return false;
        if (!description.equals(newsItem.description)) return false;
        if (imageUrl != null ? !imageUrl.equals(newsItem.imageUrl) : newsItem.imageUrl != null)
            return false;
        if (!link.equals(newsItem.link)) return false;
        return formattedDate.equals(newsItem.formattedDate);

    }

    @Override
    public int hashCode() {
        int result = itemId;
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + (int) (pubDate ^ (pubDate >>> 32));
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + link.hashCode();
        result = 31 * result + formattedDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "itemId=" + itemId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pubDate=" + pubDate +
                ", imageUrl='" + imageUrl + '\'' +
                ", link='" + link + '\'' +
                ", formattedDate='" + formattedDate + '\'' +
                '}';
    }
}
