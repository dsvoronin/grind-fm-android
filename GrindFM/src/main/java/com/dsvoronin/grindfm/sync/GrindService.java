package com.dsvoronin.grindfm.sync;

import com.dsvoronin.grindfm.entities.RSS;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GrindService {

    @GET("http://www.grind.fm/rss.xml")
    Call<RSS> getGrindFeed();
}
