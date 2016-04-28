package fm.grind.android.utils;

import android.text.Html;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import fm.grind.android.entities.CurrentTrack;
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
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {

        if (!(type instanceof Class<?>)) {
            return null;
        }

        Class<?> c = (Class<?>) type;

        if (!CurrentTrack.class.isAssignableFrom(c)) {
            return null;
        }

        return new Converter<ResponseBody, CurrentTrack>() {
            @Override
            public CurrentTrack convert(ResponseBody value) throws IOException {
                try {
                    String stringBody = value.string();
                    String[] array = stringBody.trim()
                            .substring(stringBody.indexOf(",,") + 2)
                            .split(" - ");
                    return new CurrentTrack(
                            Html.fromHtml(array[1]).toString(),
                            Html.fromHtml(array[0]).toString());
                } catch (Throwable e) {
                    throw new IOException(e);
                }
            }
        };
    }
}
