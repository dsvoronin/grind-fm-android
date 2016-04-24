package com.dsvoronin.grindfm.sync;

import com.dsvoronin.grindfm.entities.RSS;
import com.dsvoronin.grindfm.entities.Track;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GrindService {

    @GET("http://www.grind.fm/rss.xml")
    Call<RSS> getGrindFeed();

    @GET("http://radio.goha.ru:8000/7.html")
    Call<Track> getCurrentSong();
}
