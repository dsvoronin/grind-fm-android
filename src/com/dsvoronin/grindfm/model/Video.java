package com.dsvoronin.grindfm.model;

import com.dsvoronin.grindfm.util.StringUtil;

import java.util.Date;

public class Video {

    private String url;

    private String thumb;

    private Date date;

    private String title;

    public Video() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getDate() {
        return StringUtil.formatDate(date);
    }

    public void setDate(String date) {
        this.date = StringUtil.parseDate(date);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
