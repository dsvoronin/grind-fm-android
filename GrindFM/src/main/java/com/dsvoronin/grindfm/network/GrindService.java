package com.dsvoronin.grindfm.network;

import com.dsvoronin.grindfm.network.rss.RSS;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GrindService {

    @GET("http://www.grind.fm/rss.xml")
    Call<RSS> getGrindFeed();
}
