package co.nordprojects.lantern.channels.nowplaying

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nordprojects.lantern.Channel
import co.nordprojects.lantern.R
import java.io.IOException
import kotlinx.android.synthetic.main.now_playing_channel.*
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

class NowPlayingChannel() : Channel() {
    val TAG = this::class.java.simpleName
    var mediaStatus: CastConnection.MediaStatus? = null
    var mediaStatusUpdateDate: Date? = null

    var updateTimer: Timer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.now_playing_channel, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update()
    }

    override fun onStart() {
        super.onStart()

        discoveryManager.discoverServices(
                "_googlecast._tcp",
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
        )

        updateTimer = fixedRateTimer("$this update", true, Date(), 1000) {
            Handler(Looper.getMainLooper()).post {
                update()
            }
        }
    }

    override fun onStop() {
        updateTimer?.cancel()
        updateTimer = null
        discoveryManager.stopServiceDiscovery(discoveryListener)
        availableCastServices.clear()

        super.onStop()
    }

    private fun update() {
        val mediaStatus = mediaStatus
        val mediaStatusUpdateDate = mediaStatusUpdateDate
        var trackTimeEstimate: Double? = mediaStatus?.currentTime

        // if the track has been playing since the last update, keep incrementing the play time
        if (trackTimeEstimate != null
                && mediaStatusUpdateDate != null
                && mediaStatus?.playerState == CastConnection.PlayerState.PLAYING) {

            val timeSinceLastUpdateMs = Date().time - mediaStatusUpdateDate.time
            trackTimeEstimate += timeSinceLastUpdateMs / 1000

            // cap the time at the track duration as reported
            if (mediaStatus.duration != null) {
                trackTimeEstimate = min(trackTimeEstimate, mediaStatus.duration)
            }
        }

        titleTextView.text = mediaStatus?.title ?: ""
        artistTextView.text = mediaStatus?.artist ?: ""
        durationTextView.text = if (trackTimeEstimate != null) {
            // round to the nearest second (to match other players)
            trackTimeEstimate = round(trackTimeEstimate)
            // split into minutes and seconds display
            val minutes = floor(trackTimeEstimate / 60).toInt()
            val seconds = floor(trackTimeEstimate % 60).toInt()
            "%d:%02d".format(minutes, seconds)
        } else {
            ""
        }
    }

    val discoveryManager: NsdManager by lazy {
        this.context.getSystemService(NsdManager::class.java) as NsdManager
    }
    val availableCastServices = mutableListOf<NsdServiceInfo>()

    val discoveryListener = object : NsdManager.DiscoveryListener {
        val TAG = this::class.java.simpleName
        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            discoveryManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    availableCastServices.add(serviceInfo)
                    connectToAvailableCastDevice()
                }
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "Resolve error $errorCode for service $serviceInfo")
                }
            })
        }
        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            availableCastServices.remove(serviceInfo)
        }
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.w(TAG, "Start discovery error $errorCode for serviceType $serviceType")
        }
        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.w(TAG, "Stop discovery error $errorCode for serviceType $serviceType")
        }
        override fun onDiscoveryStarted(serviceType: String?) {}
        override fun onDiscoveryStopped(serviceType: String?) {}
    }

    val castConnectionListener = object : CastConnection.Listener() {
        override fun onStatusUpdate(appName: String?, status: String?) {
            Log.i(TAG, "onStatusUpdate $status")
        }
        override fun onMediaStatusUpdate(mediaStatus: CastConnection.MediaStatus) {
            Log.i(TAG, "mediaStatusUpdate $mediaStatus")

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

        Log.d(TAG, "available cast services: $availableCastServices")

        val filteredServices = availableCastServices.filter {
            it.serviceName.contains("0307")
        }

        val service = filteredServices.lastOrNull() ?: return

        connectToCastDevice(service.host.hostAddress, service.port)
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
}
