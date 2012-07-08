package com.dsvoronin.grindfm.model;

import com.dsvoronin.grindfm.util.StringUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.Date;

public class Video {

    @SerializedName("id")
    private String url;

    @SerializedName("thumbnail")
    private Thumbnail thumb;

    @SerializedName("uploaded")
    private Date date;

    @SerializedName("title")
    private String title;

    public Video() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Thumbnail getThumb() {
        return thumb;
    }

    public void setThumb(Thumbnail thumb) {
        this.thumb = thumb;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String jsonString = jsonElement.getAsJsonPrimitive().getAsString();
            return StringUtil.parseYoutubeDate(jsonString);
        }
    }
}
