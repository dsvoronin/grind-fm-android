package fm.grind.android.sync;

import fm.grind.android.entities.dto.RSS;
import fm.grind.android.entities.CurrentTrack;
import fm.grind.android.entities.TrackInList;

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
