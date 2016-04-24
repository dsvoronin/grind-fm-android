package com.dsvoronin.grindfm.utils;

import com.dsvoronin.grindfm.entities.Track;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class CurrentTrackConverterFactory extends Converter.Factory {

    public static CurrentTrackConverterFactory create() {
        return new CurrentTrackConverterFactory();
    }

    private CurrentTrackConverterFactory() {
    }

    @Override
    public Converter<ResponseBody, Track> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<ResponseBody, Track>() {
            @Override
            public Track convert(ResponseBody value) throws IOException {
                try {
                    String stringBody = value.string();
                    String[] array = stringBody.trim()
                            .substring(stringBody.indexOf(",,") + 2)
                            .split(" - ");
                    return new Track(array[1], array[0]);
                } catch (Throwable e) {
                    throw new IOException(e);
                }
            }
        };
    }
}
