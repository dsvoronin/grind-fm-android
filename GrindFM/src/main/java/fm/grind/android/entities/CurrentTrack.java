package fm.grind.android.entities;

public class CurrentTrack {

    public static final CurrentTrack NULL = new CurrentTrack("gaming radiostation", "Grind.FM");

    private final String artist;

    private final String track;

    public CurrentTrack(String artist, String track) {
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
