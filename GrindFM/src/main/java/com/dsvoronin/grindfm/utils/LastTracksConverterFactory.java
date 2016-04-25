package com.dsvoronin.grindfm.utils;

import com.dsvoronin.grindfm.entities.TrackInList;
import com.dsvoronin.grindfm.entities.dto.Track;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class LastTracksConverterFactory extends Converter.Factory {

    public static LastTracksConverterFactory create() {
        return new LastTracksConverterFactory(new Gson());
    }

    public static LastTracksConverterFactory create(Gson gson) {
        return new LastTracksConverterFactory(gson);
    }

    private final Gson gson;

    private LastTracksConverterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {

        TypeToken<List<TrackInList>> typeToken = new TypeToken<List<TrackInList>>() {
        };
        Type expectedType = typeToken.getType();

        if (!type.equals(expectedType)) {
            return null;
        }

        return new Converter<ResponseBody, List<TrackInList>>() {

            TypeToken<List<Track>> typeToken = new TypeToken<List<Track>>() {
            };

            final TypeAdapter<List<Track>> adapter = gson.getAdapter(typeToken);

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            @Override
            public List<TrackInList> convert(ResponseBody value) throws IOException {
                String string = value.string();
                String substring = string.substring(string.indexOf("=[") + 1, string.indexOf("];") + 1);
                JsonReader jsonReader = gson.newJsonReader(new StringReader(substring));
                try {
                    List<Track> read = adapter.read(jsonReader);
                    List<TrackInList> tracksInList = new ArrayList<>(7);
                    for (Track track : read.subList(1, 7)) {

                        String timestampString = track.dt.substring(0, track.dt.indexOf('.'));
                        Long timestamp = Long.valueOf(timestampString) * 1000;
                        String formattedDate = dateFormat.format(new Date(timestamp));

                        tracksInList.add(new TrackInList(
                                formattedDate,
                                track.artist + " - " + track.title));
                    }
                    return tracksInList;
                } finally {
                    value.close();
                }
            }
        };
    }
}
