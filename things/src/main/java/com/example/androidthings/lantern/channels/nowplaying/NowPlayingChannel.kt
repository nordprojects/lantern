package com.example.androidthings.lantern.channels.nowplaying

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.Channel
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.shared.CastDiscoveryManager
import kotlinx.android.synthetic.main.now_playing_channel_stacked.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round
import kotlin.properties.Delegates


/**
 * Shows the currently playing song on a Cast device on the local network.
 *
 * Config parameters:
 *   - "castId"
 *         The ID of the cast device to observe. If null, the cast device will observe the
 *         first device it finds on the network.
 */
class NowPlayingChannel : Channel() {
    val TAG = this::class.java.simpleName
    private val configDeviceId by lazy { config.settings.opt("castId") as? String }
    private val configStyle by lazy {
        Style.withJsonName(config.settings.optString("style")) ?: Style.STACKED
    }

    var mediaStatus: CastConnection.MediaStatus? = null
    var mediaStatusUpdateDate: Date? = null

    val discoveryManager by lazy { CastDiscoveryManager(context!!) }
    var updateTimer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return when (configStyle) {
            Style.STACKED ->
                inflater.inflate(R.layout.now_playing_channel_stacked, container, false)
            Style.SCROLLING ->
                inflater.inflate(R.layout.now_playing_channel_scrolling, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tickerView = view.findViewById<NowPlayingTickerView>(R.id.tickerView)
        tickerView?.startAnimating()
        update()
    }

    override fun onStart() {
        super.onStart()

        discoveryManager.listener = object : CastDiscoveryManager.Listener {
            override fun devicesUpdated() {
                connectToAvailableCastDevice()
            }
        }
        discoveryManager.startDiscovering()

        updateTimer = fixedRateTimer("$this update", true, Date(), 100) {
            Handler(Looper.getMainLooper()).post {
                update()
            }
        }
    }

    override fun onStop() {
        updateTimer?.cancel()
        updateTimer = null
        discoveryManager.listener = null
        discoveryManager.stopDiscovering()

        super.onStop()
    }

    private fun update() {
        if (view == null) {
            return
        }

        val mediaStatus = mediaStatus
        val mediaStatusUpdateDate = mediaStatusUpdateDate
        var trackTimeEstimate: Double? = mediaStatus?.currentTime

        // if the track has been playing since the last update, keep incrementing the play time
        if (trackTimeEstimate != null
                && mediaStatusUpdateDate != null
                && mediaStatus?.playerState == CastConnection.PlayerState.PLAYING) {

            val timeSinceLastUpdateMs = Date().time - mediaStatusUpdateDate.time
            trackTimeEstimate += timeSinceLastUpdateMs / 1000.0

            // cap the time at the track duration as reported
            if (mediaStatus.duration != null) {
                trackTimeEstimate = min(trackTimeEstimate, mediaStatus.duration)
            }
        }

        val title = mediaStatus?.title ?: ""
        val subtitle = mediaStatus?.artist ?: mediaStatus?.subtitle ?: ""
        val elapsedTime = if (trackTimeEstimate != null) {
            // round to the nearest second (to match other players)
            val totalSeconds = round(trackTimeEstimate)
            // split into minutes and seconds display
            val minutes = floor(totalSeconds / 60).toInt()
            val seconds = floor(totalSeconds % 60).toInt()
            "%d:%02d".format(minutes, seconds)
        } else {
            ""
        }

        when (configStyle) {
            Style.STACKED -> {
                titleTextView.text = title
                artistTextView.text = subtitle
                durationTextView.text = elapsedTime

                val duration = mediaStatus?.duration
                progressBarView.scaleX =
                        if (trackTimeEstimate != null && duration != null) {
                            (trackTimeEstimate / duration).toFloat()
                        } else {
                            0f
                        }
            }
            Style.SCROLLING -> {
//                val text = SpannableStringBuilder()
//                        .append(title, StyleSpan(BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        .append("    ")
//                        .append(subtitle)
//                        .append("    ")
//                        .append(elapsedTime)
//                        .append("    ")

                val tickerView = view!!.findViewById<NowPlayingTickerView>(R.id.tickerView)

                tickerView.title = title
                tickerView.subtitle = subtitle
                tickerView.duration = elapsedTime
            }
        }
    }

    val castConnectionListener = object : CastConnection.Listener() {
        override fun onStatusUpdate(appName: String?, status: String?) {
        }

        override fun onMediaStatusUpdate(mediaStatus: CastConnection.MediaStatus) {
            this@NowPlayingChannel.mediaStatus = mediaStatus
            mediaStatusUpdateDate = Date()
            update()
        }

        override fun onDisconnect() {
            disconnectFromCastDevice()
            connectToAvailableCastDevice()
        }
    }

    fun connectToAvailableCastDevice() {
        if (castConnection != null) {
            // we're already connected, there's nothing to do.
            return
        }

        Log.d(TAG, "Available cast devices: ${discoveryManager.devices}")

        val filteredDevices = discoveryManager.devices.filter {
            if (configDeviceId == null)
                true
            else
                it.id == configDeviceId
        }

        val device = filteredDevices.lastOrNull() ?: return

        connectToCastDevice(device.hostString, device.port)
    }

    var castConnection: CastConnection? = null

    fun connectToCastDevice(host: String, port: Int) {
        val connection = CastConnection(host, port)
        connection.listener = castConnectionListener
        castConnection = connection

        Thread(Runnable {
            try {
                connection.connect()
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to cast device", e)
                Handler(Looper.getMainLooper()).post {
                    disconnectFromCastDevice()
                    connectToAvailableCastDevice()
                }
            }
        }).start()
    }

    fun disconnectFromCastDevice() {
        castConnection?.listener = null
        castConnection?.close()
        castConnection = null
    }

    enum class Style {
        STACKED, SCROLLING;

        val jsonName
            get() = when (this) {
                STACKED -> "stacked"
                SCROLLING -> "scrolling"
            }

        companion object {
            fun withJsonName(jsonName: String): Style? {
                return Style.values().find { it.jsonName == jsonName }
            }
        }
    }

    class NowPlayingTickerView(context: Context, attrs: AttributeSet): View(context, attrs) {
        val TAG = this::class.java.simpleName

        var title: String by Delegates.observable("", { _, oldValue, newValue ->
            if (newValue != oldValue) {
                titleBitmap = null
            }
        })
        var subtitle: String by Delegates.observable("", { _, oldValue, newValue ->
            if (newValue != oldValue) {
                subtitleBitmap = null
            }
        })
        var duration: String by Delegates.observable("", { _, oldValue, newValue ->
            if (newValue != oldValue) {
                durationBitmap = null
            }
        })

        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = resources.getFont(R.font.lato_black_italic)
            textSize = 80F
            color = Color.WHITE
        }
        private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = resources.getFont(R.font.lato_italic)
            textSize = 80F
            color = Color.WHITE
        }
        private val durationPaint = subtitlePaint

        private var titleBitmap: Bitmap? = null
        private var subtitleBitmap: Bitmap? = null
        private var durationBitmap: Bitmap? = null

        private var scrollX = 0f
        private val SCROLL_SPEED = 80f // pixels per second
        private val ELEMENT_GAP_WIDTH = 75f
        private val REPEAT_GAP_WIDTH = 250f
        private var drawTimer: TimeAnimator? = null

        override fun onVisibilityAggregated(isVisible: Boolean) {
            super.onVisibilityAggregated(isVisible)

            if (isVisible) {
                startAnimating()
            } else {
                stopAnimating()
            }
        }

        fun stopAnimating() {
            drawTimer?.cancel()
            drawTimer = null
        }

        fun startAnimating() {
            stopAnimating()
            drawTimer = TimeAnimator().apply {
                setTimeListener { _, _, delta ->
                    tick(delta)
                }
                start()
            }
        }

        private fun tick(delta: Long) {
            scrollX += SCROLL_SPEED * (delta.toFloat() / 1000f)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (titleBitmap == null) {
                titleBitmap = renderTextToBitmap(title.toUpperCase(), titlePaint)
            }
            if (subtitleBitmap == null) {
                subtitleBitmap = renderTextToBitmap(subtitle.toUpperCase(), subtitlePaint)
            }
            if (durationBitmap == null) {
                durationBitmap = renderTextToBitmap(duration.toUpperCase(), durationPaint)
            }

            val titleBitmap = titleBitmap!!
            val subtitleBitmap = subtitleBitmap!!
            val durationBitmap = durationBitmap!!

            val totalLineWidth = (
                    titleBitmap.width + ELEMENT_GAP_WIDTH
                            + subtitleBitmap.width + ELEMENT_GAP_WIDTH
                            + durationBitmap.width + REPEAT_GAP_WIDTH)

            var drawX = -scrollX % totalLineWidth
            drawX -= canvas.width

            while (drawX < canvas.width) {
                canvas.drawBitmap(titleBitmap, drawX, 0f, null)
                drawX += titleBitmap.width
                drawX += ELEMENT_GAP_WIDTH
                canvas.drawBitmap(subtitleBitmap, drawX, 0f, null)
                drawX += subtitleBitmap.width
                drawX += ELEMENT_GAP_WIDTH
                canvas.drawBitmap(durationBitmap, drawX, 0f, null)
                drawX += durationBitmap.width
                drawX += REPEAT_GAP_WIDTH
            }
        }

        companion object {
            fun renderTextToBitmap(text: String, paint: Paint): Bitmap {
                val baseline = -paint.ascent() // ascent() is negative
                val width = ceil(paint.measureText(text)).toInt() + 1
                val height = ceil(baseline + paint.descent()).toInt()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawText(text, 0f, baseline, paint)
                return bitmap
            }
        }
    }
}
