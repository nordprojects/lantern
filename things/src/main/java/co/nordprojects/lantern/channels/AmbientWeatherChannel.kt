package co.nordprojects.lantern.channels

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.Channel
import co.nordprojects.lantern.R
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.android.synthetic.main.ambient_weather_channel.*
import java.util.*
import kotlin.concurrent.fixedRateTimer


/**
 * Shows an ambient video representing the weather at a location.
 *
 * Requires an OpenWeatherMap API key at R.string.openweathermap_api_key to work.
 *
 * Config parameters:
 *   - "latitude"
 *   - "longitude"
 *         The location to get the weather for. If missing, uses the Googleplex.
 */
class AmbientWeatherChannel : Channel() {
    val TAG = this::class.java.simpleName
    var weatherConditions: WeatherConditions? = null
    var videoUri: Uri? = null

    var refreshTimer: Timer? = null
    var refreshError: Exception? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.ambient_weather_channel, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoView.setOnPreparedListener({
            it.isLooping = true
        })

        update()
    }

    override fun onStart() {
        super.onStart()

        refreshTimer = fixedRateTimer("$this refresh", true, Date(), 30000) {
            Handler(Looper.getMainLooper()).post {
                refreshData()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        refreshTimer?.cancel()
        refreshTimer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        videoView.stopPlayback()
        videoUri = null
    }

    private fun update() {
        if (videoView == null) return

        val newVideoUri = weatherConditions?.videoUrl

        if (videoUri != newVideoUri) {
            videoUri = newVideoUri
            if (videoUri != null) {
                videoView.setVideoURI(videoUri)
                videoView.start()
            } else {
                videoView.stopPlayback()
            }
        }

        statusTextView?.text = if (videoUri == null && refreshError == null) {
            "Loading..."
        } else if (videoUri == null && refreshError != null) {
            "An error occurred while getting the current weather.\n\n$refreshError"
        } else {
            ""
        }
    }

    private fun refreshData() {
        var latitude = config.settings.opt("latitude") as? Double
        var longitude = config.settings.opt("longitude") as? Double

        if (latitude == null || longitude == null) {
            // use the Googleplex coordinates
            latitude = 37.422
            longitude = -122.086
        }
        RefreshWeatherTask().execute(latitude, longitude)
    }

    inner class RefreshWeatherTask : AsyncTask<Double, Void, WeatherConditions?>() {
        var doInBackgroundError: Exception? = null

        override fun doInBackground(vararg params: Double?): WeatherConditions? {
            try {
                val latitude = params[0]!!
                val longitude = params[1]!!

                val weatherJson = fetchWeatherData(latitude, longitude)
                val weatherConditionsId = weatherJson
                        .getJSONArray("weather")
                        .getJSONObject(0)
                        .getInt("id")
                return WeatherConditions.valueWithOpenweathermapId(weatherConditionsId)
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to refresh weather $e", e)
                doInBackgroundError = e
                return null
            }
        }

        override fun onPostExecute(result: WeatherConditions?) {
            if (result != null) {
                weatherConditions = result
            }

            refreshError = doInBackgroundError

            update()
        }

        private fun fetchWeatherData(latitude: Double, longitude: Double): JSONObject {
            var connection: HttpsURLConnection? = null
            var inputStream: InputStream? = null
            val apiKey = context.resources.getString(R.string.openweathermap_api_key)
            val url = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey")

            try {
                connection = url.openConnection() as HttpsURLConnection
                connection.connect()
                if (connection.responseCode !in 200..299) {
                    throw IOException("HTTP error ${connection.responseCode}")
                }

                inputStream = connection.inputStream
                val jsonString = inputStream.reader().readText()
                return JSONObject(jsonString)
            }
            finally {
                inputStream?.close()
                connection?.disconnect()
            }
        }
    }

    enum class WeatherConditions {
        CALM, SUNNY, RAIN, HEAVY_RAIN, GENTLE_WIND, WINDY;

        val videoUrl: Uri get() {
            return when(this) {
                CALM -> Uri.parse("https://s3.amazonaws.com/lantern-resources/calm.mp4")
                SUNNY -> Uri.parse("https://s3.amazonaws.com/lantern-resources/sunny.mp4")
                RAIN -> Uri.parse("https://s3.amazonaws.com/lantern-resources/rain.mp4")
                HEAVY_RAIN -> Uri.parse("https://s3.amazonaws.com/lantern-resources/heavy-rain.mp4")
                GENTLE_WIND -> Uri.parse("https://s3.amazonaws.com/lantern-resources/gentle-wind.mp4")
                WINDY -> Uri.parse("https://s3.amazonaws.com/lantern-resources/windy.mp4")
            }
        }

        companion object {
            val TAG = WeatherConditions::class.java.simpleName

            fun valueWithOpenweathermapId(id: Int): WeatherConditions {
                return when (id) {
                    200 -> RAIN // thunderstorm with light rain
                    201 -> RAIN // thunderstorm with rain
                    202 -> HEAVY_RAIN // thunderstorm with heavy rain
                    210 -> RAIN // light thunderstorm
                    211 -> RAIN // thunderstorm
                    212 -> HEAVY_RAIN // heavy thunderstorm
                    221 -> HEAVY_RAIN // ragged thunderstorm
                    230 -> RAIN // thunderstorm with light drizzle
                    231 -> RAIN // thunderstorm with drizzle
                    232 -> HEAVY_RAIN // thunderstorm with heavy drizzle

                    300 -> RAIN // light intensity drizzle
                    301 -> RAIN // drizzle
                    302 -> HEAVY_RAIN // heavy intensity drizzle
                    310 -> RAIN // light intensity drizzle rain
                    311 -> RAIN // drizzle rain
                    312 -> HEAVY_RAIN // heavy intensity drizzle rain
                    313 -> RAIN // shower rain and drizzle
                    314 -> HEAVY_RAIN // heavy shower rain and drizzle
                    321 -> RAIN // shower drizzle

                    500 -> RAIN // light rain
                    501 -> RAIN // moderate rain
                    502 -> HEAVY_RAIN // heavy intensity rain
                    503 -> HEAVY_RAIN // very heavy rain
                    504 -> HEAVY_RAIN // extreme rain
                    511 -> RAIN // freezing rain
                    520 -> RAIN // light intensity shower rain
                    521 -> HEAVY_RAIN // shower rain
                    522 -> HEAVY_RAIN // heavy intensity shower rain
                    531 -> RAIN // ragged shower rain

                    600 -> CALM // light snow
                    601 -> CALM // snow
                    602 -> CALM // heavy snow
                    611 -> CALM // sleet
                    612 -> CALM // shower sleet
                    615 -> CALM // light rain and snow
                    616 -> CALM // rain and snow
                    620 -> CALM // light shower snow
                    621 -> CALM // shower snow
                    622 -> CALM // heavy shower snow

                    701 -> CALM // mist
                    711 -> CALM // smoke
                    721 -> CALM // haze
                    731 -> CALM // sand, dust whirls
                    741 -> CALM // fog
                    751 -> CALM // sand
                    761 -> CALM // dust
                    762 -> CALM // volcanic ash
                    771 -> CALM // squalls
                    781 -> CALM // tornado

                    800 -> SUNNY // clear sky
                    801 -> SUNNY // few clouds
                    802 -> CALM // scattered clouds
                    803 -> CALM // broken clouds
                    804 -> CALM // overcast clouds

                    900 -> WINDY // tornado
                    901 -> WINDY // tropical storm
                    902 -> WINDY // hurricane
                    903 -> CALM // cold
                    904 -> CALM // hot
                    905 -> WINDY // windy
                    906 -> CALM // hail
                    951 -> CALM // calm
                    952 -> GENTLE_WIND // light breeze
                    953 -> GENTLE_WIND // gentle breeze
                    954 -> GENTLE_WIND // moderate breeze
                    955 -> GENTLE_WIND // fresh breeze
                    956 -> GENTLE_WIND // strong breeze
                    957 -> WINDY // high wind, near gale
                    958 -> WINDY // gale
                    959 -> WINDY // severe gale
                    960 -> WINDY // storm
                    961 -> WINDY // violent storm
                    962 -> WINDY // hurricane
                    else -> {
                        Log.w(TAG, "Unknown weather id $id, falling back to CALM")
                        CALM
                    }
                }
            }
        }
    }
}
