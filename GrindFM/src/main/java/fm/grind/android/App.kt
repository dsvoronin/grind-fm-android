package fm.grind.android

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.leakcanary.LeakCanary
import com.squareup.picasso.Picasso
import fm.grind.android.sync.GrindService
import fm.grind.android.utils.CurrentTrackConverterFactory
import fm.grind.android.utils.LastTracksConverterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class App : Application() {

    var picasso: Picasso? = null
        private set

    var grindService: GrindService? = null
        private set

    private var tracker: Tracker? = null

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        LeakCanary.install(this)

        val cacheDir = getDir("okhttp_cache", Context.MODE_PRIVATE)

        val okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(StethoInterceptor())
                .cache(Cache(cacheDir, CACHE_SIZE.toLong()))
                .build()

        picasso = Picasso.Builder(this)
                .downloader(OkHttp3Downloader(okHttpClient))
                .build()

        Picasso.setSingletonInstance(picasso)

        val locale = resources.configuration.locale

        grindService = Retrofit.Builder()
                .baseUrl("http://grind.fm")
                .client(okHttpClient)
                .addConverterFactory(CurrentTrackConverterFactory.create())
                .addConverterFactory(LastTracksConverterFactory.create(locale))
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
                .create<GrindService>(GrindService::class.java)
    }

    /**
     * Gets the default [Tracker] for this [Application].

     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    val defaultTracker: Tracker
        @Synchronized get() {
            if (tracker == null) {
                val analytics = GoogleAnalytics.getInstance(this)
                tracker = analytics.newTracker(R.xml.global_tracker)
            }
            return tracker!!
        }

    companion object {

        /**
         * 300mb cache
         */
        private val CACHE_SIZE = 1024 * 1024 * 300

        fun fromContext(context: Context): App {
            return context.applicationContext as App
        }
    }
}
