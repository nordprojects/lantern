package com.example.androidthings.lantern.channels.nowplaying

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
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
import kotlinx.android.synthetic.main.now_playing_channel.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer
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

    var mediaStatus: CastConnection.MediaStatus? = null
    var mediaStatusUpdateDate: Date? = null

    val discoveryManager by lazy { CastDiscoveryManager(context!!) }
    var updateTimer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.now_playing_channel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        titleTextView.text = mediaStatus?.title ?: ""
        artistTextView.text = mediaStatus?.artist ?: mediaStatus?.subtitle ?: ""
        durationTextView.text = if (trackTimeEstimate != null) {
            // round to the nearest second (to match other players)
            val totalSeconds = round(trackTimeEstimate)
            // split into minutes and seconds display
            val minutes = floor(totalSeconds / 60).toInt()
            val seconds = floor(totalSeconds % 60).toInt()
            "%d:%02d".format(minutes, seconds)
        } else {
            ""
        }
        val duration = mediaStatus?.duration

        progressBarView.scaleX =
                if (trackTimeEstimate != null && duration != null) {
                    (trackTimeEstimate / duration).toFloat()
                } else {
                    0f
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
            }
            catch (e: IOException) {
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

    class ProgressBarView(context: Context, attrs: AttributeSet): View(context, attrs) {
        var barColor: Int by Delegates.observable(Color.WHITE, { _, _, newValue ->
            barPaint = Paint().apply {
                color = newValue
            }
        })
        private var barPaint: Paint = Paint().apply {
            color = barColor
        }

        var percentComplete: Double by Delegates.observable(0.0, {
            _, oldValue, newValue ->
            if (oldValue != newValue) {
                invalidate()
            }
        })

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            canvas.drawRect(
                    0f,
                    0f,
                    (canvas.width * percentComplete/100.0).toFloat(),
                    canvas.height.toFloat(),
                    barPaint
            )
        }
    }
}
