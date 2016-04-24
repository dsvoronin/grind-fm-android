package com.dsvoronin.grindfm.entities;

public class TrackListItem {

    private String date;

    private String track;

    public TrackListItem() {
    }

    public TrackListItem(String date, String track) {
        this.date = date;
        this.track = track;
    }

    public String getDate() {
        return date;
    }

    public String getTrack() {
        return track;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackListItem that = (TrackListItem) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (track != null ? !track.equals(that.track) : that.track != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (track != null ? track.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TrackListItem{" +
                "date='" + date + '\'' +
                ", track='" + track + '\'' +
                '}';
    }
}
