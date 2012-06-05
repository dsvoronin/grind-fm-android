package com.dsvoronin.grindfm.model;

import com.google.gson.annotations.SerializedName;

public class RequestSong {

    @SerializedName("int_id")
    private int id;

    @SerializedName("artist")
    private String artist;

    @SerializedName("title")
    private String title;

    @SerializedName("album")
    private String album;

    @SerializedName("duration")
    private float duaration;

    /**
     * todo: wtf?
     */
    @SerializedName("local")
    private String local;

    @SerializedName("collection")
    private String collection;

    @SerializedName("lastp")
    private String lastPlayed;

    /**
     * todo: wtf?
     */
    @SerializedName("file")
    private String file;

    public boolean isAvailable() {
        return lastPlayed == null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public float getDuaration() {
        return duaration;
    }

    public void setDuaration(float duaration) {
        this.duaration = duaration;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
