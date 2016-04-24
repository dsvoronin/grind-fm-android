package com.dsvoronin.grindfm.entities;

public class Track {

    private final String artist;

    private final String track;

    public Track(String artist, String track) {
        this.artist = artist;
        this.track = track;
    }

    public String getArtist() {
        return artist;
    }

    public String getTrack() {
        return track;
    }
}
