package fm.grind.android.utils

import com.github.salomonbrys.kotson.typeToken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fm.grind.android.entities.TrackInList
import fm.grind.android.entities.dto.Track
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.StringReader
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class LastTracksConverterFactory(private val locale: Locale,
                                 private val gson: Gson = Gson()) : Converter.Factory() {

    override fun responseBodyConverter(type: Type,
                                       annotations: Array<Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *>? {

        if (type != typeToken<List<TrackInList>>()) {
            return null
        }

        return TrackInListConverter(gson, locale)
    }

    class TrackInListConverter(private val gson: Gson,
                               locale: Locale) : Converter<ResponseBody, List<TrackInList>> {

        internal val adapter = gson.getAdapter<List<Track>>(object : TypeToken<List<Track>>() {})

        internal var dateFormat = SimpleDateFormat("HH:mm", locale)

        override fun convert(value: ResponseBody): List<TrackInList> {
            value.use {
                val string = value.string()
                val substring = string.substring(string.indexOf("=[") + 1, string.indexOf("];") + 1)
                val jsonReader = gson.newJsonReader(StringReader(substring))

                val read = adapter.read(jsonReader)
                val tracksInList = ArrayList<TrackInList>(7)
                for (track in read.subList(1, 7)) {

                    val timestampString = track.dt!!.substring(0, track.dt!!.indexOf('.'))
                    val timestamp = java.lang.Long.valueOf(timestampString)!! * 1000
                    val formattedDate = dateFormat.format(Date(timestamp))

                    tracksInList.add(TrackInList(
                            formattedDate,
                            track.artist + " - " + track.title))
                }
                return tracksInList
            }
        }
    }
}
