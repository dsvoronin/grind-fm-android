package com.dsvoronin.grindfm.entities;

public class TrackListItem {

    private String date;

    private String artist;

    private String title;

    private String duration;

    public TrackListItem() {
    }

    public TrackListItem(String date, String artist, String title, String duration) {
        this.date = date;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackListItem item = (TrackListItem) o;

        if (!artist.equals(item.artist)) return false;
        if (!title.equals(item.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = artist.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TrackListItem{" +
                "date='" + date + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
