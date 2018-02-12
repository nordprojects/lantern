package co.nordprojects.lantern.channels

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biweekly.Biweekly
import biweekly.component.VEvent
import co.nordprojects.lantern.Channel
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.fixedRateTimer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CalendarChannel() : Channel() {
    val TAG = this::class.java.simpleName
    var events = listOf<VEvent>()
    var refreshEventsTimer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return object : View(context) {
            val whitePaint = Paint().apply {
                color = Color.WHITE
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)

                val segmentRadius = canvas.height/2.0 * 0.7
                canvas.drawColor(Color.BLACK)
                val calendar = Calendar.getInstance()

                for (event in events) {
                    val startDate = event.dateStart.value
                    calendar.time = startDate
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    val angle = (hour + minute/60.0) * 2.0*PI / 12.0

                    val x = canvas.width/2.0F + segmentRadius * sin(angle)
                    val y = canvas.height/2.0F - segmentRadius * cos(angle)
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 10F, whitePaint)
                    canvas.drawText(event.summary.value, x.toFloat(), y.toFloat(), whitePaint)
                }
            }
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update()
    }

    override fun onStart() {
        super.onStart()
        refreshEventsTimer = fixedRateTimer("$this refresh", false, Date(), 10000) {
            Handler(Looper.getMainLooper()).post({
                refreshEvents()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "resume")
    }

    override fun onStop() {
        refreshEventsTimer?.cancel()
        refreshEventsTimer = null
        super.onStop()
    }

    fun refreshEvents() {
        val url = URL(config.settings.getString("url"))
        RefreshEventsTask().execute(url)
    }

    fun update() {
        Log.d(TAG, "Events are $events")
        view?.invalidate()
    }

    inner class RefreshEventsTask : AsyncTask<URL, Void, List<VEvent>?>() {
        val TAG = this::class.java.simpleName

        override fun doInBackground(vararg params: URL?): List<VEvent>? {
            val url = params.first()!!

            val events = downloadEventsFromURL(url) ?: return null
            return filteredEventsWithin12Hours(events, Date())
        }

        override fun onPostExecute(result: List<VEvent>?) {
            if (result == null) {
                Log.e(TAG, "null result from RefreshEventsTask")
                return
            }
            events = result
            update()
        }

        private fun filteredEventsWithin12Hours(events: List<VEvent>, date: Date): List<VEvent>? {
            val result = mutableListOf<VEvent>()
            val startDate = Date(date.time - 6*60*60*1000)
            val endDate = Date(date.time + 6*60*60*1000)

            for (event in events) {
                val dateIterator = event.getDateIterator(TimeZone.getDefault())
                dateIterator.advanceTo(startDate)

                // if the next occurrence is before the endDate, include this event
                if (dateIterator.hasNext()) {
                    val nextOccurrence = dateIterator.next()
                    if (nextOccurrence.before(endDate)) {
                        result.add(event)
                    }
                }
            }

            return result
        }

        private fun downloadEventsFromURL(url: URL): List<VEvent>? {
            var connection: HttpsURLConnection? = null
            var inputStream: InputStream? = null

            try {
                connection = url.openConnection() as HttpsURLConnection
                connection.connect()
                if (connection.responseCode !in 200..299) {
                    Log.e(TAG, "HTTP error ${connection.responseCode}")
                    return null
                }

                inputStream = connection.inputStream
                val calendar = Biweekly.parse(inputStream).first()
                return calendar.events
            }
            finally {
                inputStream?.close()
                connection?.disconnect()
            }
        }
    }
}