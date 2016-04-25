package com.dsvoronin.grindfm.sync;

import com.dsvoronin.grindfm.entities.dto.RSS;
import com.dsvoronin.grindfm.entities.CurrentTrack;
import com.dsvoronin.grindfm.entities.TrackInList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GrindService {

    @GET("http://www.grind.fm/rss.xml")
    Call<RSS> getGrindFeed();

    @GET("http://radio.goha.ru:8000/7.html")
    Call<CurrentTrack> getCurrentSong();

    @GET("http://media.goha.ru/radio/meta2.php")
    Call<List<TrackInList>> getLastPlayedTracks();
}
