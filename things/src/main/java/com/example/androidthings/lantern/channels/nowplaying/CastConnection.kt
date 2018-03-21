package com.example.androidthings.lantern.channels.nowplaying

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import extensions.api.cast_channel.CastChannel
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.fixedRateTimer

/**
 * Communicates with a Cast device directly. Gets the media status of the Cast
 * device including track metadata. Provides callbacks to the listener when the
 * media changes.
 */
class CastConnection(val host: String, val port: Int) {
    enum class PlayerState { IDLE, BUFFERING, PLAYING, PAUSED }
    data class MediaStatus(val title: String?,
                           val subtitle: String?,
                           val artist: String?,
                           val album: String?,
                           val duration: Double?,
                           val currentTime: Double?,
                           val playerState: PlayerState?)

    @Suppress("unused", "UNUSED_PARAMETER")
    open class Listener {
        open fun onStatusUpdate(appName: String?, status: String?) {}
        open fun onMediaStatusUpdate(mediaStatus: MediaStatus) {}
        open fun onDisconnect() {}
    }
    var listener: Listener? = null

    companion object {
        private val TAG: String = CastConnection::class.java.simpleName
        const val DEFAULT_RECEIVER_ID = "receiver-0"
    }

    private lateinit var transport: CastConnectionTransport
    private val transportListener = object : CastConnectionTransport.Listener {
        override fun onMessageReceived(message: CastChannel.CastMessage) {
            handleReceivedMessage(message)
        }
        override fun onDisconnect() {
            close()
        }
    }

    private val requestIdGenerator = AtomicLong(1)
    private var currentAppTransportId: String? = null

    fun connect() {
        transport = CastConnectionTransport(host, port, transportListener)

        sendConnectMessage(DEFAULT_RECEIVER_ID)
        sendGetStatusMessage()

        startHeartbeat()
    }

    var isClosed = false
    fun close() {
        if (!isClosed) {
            isClosed = true

            stopHeartbeat()
            transport.listener = null
            transport.close()

            Handler(Looper.getMainLooper()).post {
                listener?.onDisconnect()
            }
        }
    }

    //region Handling received messages

    private fun handleReceivedMessage(message: CastChannel.CastMessage) {
        if (message.namespace != "urn:x-cast:com.google.cast.tp.heartbeat") {
            //Log.d(TAG, "Received message $message")
        }

        if (message.payloadType != CastChannel.CastMessage.PayloadType.STRING) {
            return
        }

        val payload = JSONObject(message.payloadUtf8)

        val payloadType = payload.optString("type")

        when (payloadType) {
            "PING" -> handleReceivedPing()
            "RECEIVER_STATUS" -> handleReceivedStatus(payload)
            "MEDIA_STATUS" -> handleReceivedMediaStatus(payload)
        }
    }

    private fun handleReceivedPing() {
        sendPongMessage()
    }

    private fun handleReceivedStatus(payload: JSONObject) {
        val applicationsJson = payload.optJSONObject("status")?.optJSONArray("applications")

        if (applicationsJson == null || applicationsJson.length() == 0) {
            return
        }

        val applicationJson = applicationsJson[0] as JSONObject
        val appName = applicationJson.optString("displayName")
        val appStatus = applicationJson.optString("statusText")
        currentAppTransportId = applicationJson.optString("transportId")

        Handler(Looper.getMainLooper()).post {
            listener?.onStatusUpdate(appName, appStatus)
        }

        if (currentAppTransportId != null) {
            connectToAppIfNeeded(currentAppTransportId!!)
            sendGetMediaStatusMessage(currentAppTransportId!!)
        }
    }

    private var previousMediaStatus: MediaStatus? = null

    private fun handleReceivedMediaStatus(payload: JSONObject) {
        val status = payload.optJSONArray("status")?.optJSONObject(0)
                ?: return

        val playerState = PlayerState.valueOf(status.optString("playerState"))
        val currentTime = status.opt("currentTime") as? Double
        val media = status.optJSONObject("media")
        val metadata = media?.optJSONObject("metadata")

        val mediaStatus = if (metadata != null) {
            var artist = metadata.opt("artist") as? String
            if (artist.isNullOrEmpty()) {
                artist = metadata.opt("albumArtist") as? String
            }

            MediaStatus(
                    title = metadata.opt("title") as? String,
                    subtitle = metadata.opt("subtitle") as? String,
                    artist = artist,
                    album = metadata.opt("albumName") as? String,
                    currentTime = currentTime,
                    duration = media.opt("duration") as? Double,
                    playerState = playerState
            )
        } else {
            // if the media object isn't present, just update the variables from `status`
            previousMediaStatus?.copy(
                    currentTime = currentTime,
                    playerState = playerState
            )
        }

        if (mediaStatus != null && mediaStatus != previousMediaStatus) {
            previousMediaStatus = mediaStatus
            Handler(Looper.getMainLooper()).post {
                listener?.onMediaStatusUpdate(mediaStatus)
            }
        }
    }

    //endregion

    //region Message send methods

    private fun sendPingMessage() {
        transport.sendMessage(
                DEFAULT_RECEIVER_ID,
                "urn:x-cast:com.google.cast.tp.heartbeat",
                JSONObject(mapOf("type" to "PING"))
        )
    }

    private fun sendPongMessage() {
        transport.sendMessage(
                "urn:x-cast:com.google.cast.tp.heartbeat",
                DEFAULT_RECEIVER_ID,
                JSONObject(mapOf("type" to "PONG"))
        )
    }

    private fun sendConnectMessage(destinationId: String) {
        transport.sendMessage(
                destinationId,
                "urn:x-cast:com.google.cast.tp.connection",
                JSONObject(mapOf(
                        "type" to "CONNECT"
                ))
        )
    }

    private fun sendGetStatusMessage() {
        transport.sendMessage(
                DEFAULT_RECEIVER_ID,
                "urn:x-cast:com.google.cast.receiver",
                JSONObject(mapOf(
                        "type" to "GET_STATUS",
                        "requestId" to requestIdGenerator.getAndIncrement()
                ))
        )
    }

    private fun sendGetMediaStatusMessage(destinationId: String) {
        transport.sendMessage(
                destinationId,
                "urn:x-cast:com.google.cast.media",
                JSONObject(mapOf(
                        "type" to "GET_STATUS",
                        "requestId" to requestIdGenerator.getAndIncrement()))
        )
    }

    //endregion

    private val connectedAppTransportIds = mutableListOf<String>()

    private fun connectToAppIfNeeded(transportId: String) {
        if (!connectedAppTransportIds.contains(transportId)) {
            sendConnectMessage(transportId)
            connectedAppTransportIds.add(transportId)
        }
    }

    private var heartbeatTimer: Timer? = null

    private fun startHeartbeat() {
        heartbeatTimer = fixedRateTimer("Cast $this heartbeat", true, Date(), 1000) {
            sendPingMessage()
        }
    }

    private fun stopHeartbeat() {
        heartbeatTimer?.cancel()
        heartbeatTimer = null
    }
}

/**
 * Manages the underlying communications channel to the cast device, including
 * authentication, message serialization and deserialization.
 */
private class CastConnectionTransport(host: String,
                                      port: Int,
                                      var listener: CastConnectionTransport.Listener? = null) {
    interface Listener {
        fun onMessageReceived(message: CastChannel.CastMessage)
        fun onDisconnect()
    }

    companion object {
        private val TAG = CastConnectionTransport::class.java.simpleName!!
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(TrustAllX509TrustManager()), java.security.SecureRandom())
    }
    private val socket = sslContext.socketFactory.createSocket(host, port)
    private val inputStream = DataInputStream(socket.getInputStream())
    private val outputStream = DataOutputStream(socket.getOutputStream())
    private val sourceId = "sender-${UUID.randomUUID()}"
    private var readThread: Thread? = null

    init {
        authenticate()

        readThread = Thread(Runnable {
            while (true) {
                val message = try {
                    readMessage()
                }
                catch (e: IOException) {
                    Log.e(TAG, "Read failed. Closing connection.", e)
                    close()
                    break
                }

                listener?.onMessageReceived(message)
            }
        })
        readThread?.start()
    }

    fun sendMessage(destinationId: String, namespace: String, payload: JSONObject) {
        val message = CastChannel.CastMessage.newBuilder().also {
            it.protocolVersion = CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0
            it.sourceId = sourceId
            it.destinationId = destinationId
            it.namespace = namespace
            it.payloadType = CastChannel.CastMessage.PayloadType.STRING
            it.payloadUtf8 = payload.toString()
        }.build()

        sendMessage(message)
    }

    var isClosed = false
    fun close() {
        if (!isClosed) {
            isClosed = true

            inputStream.close()
            outputStream.close()
            socket.close()

            listener?.onDisconnect()
        }
    }

    private fun authenticate() {
        val authMessage = CastChannel.DeviceAuthMessage.newBuilder()
                .setChallenge(CastChannel.AuthChallenge.newBuilder().build())
                .build()

        val message = CastChannel.CastMessage.newBuilder()
                .setDestinationId(CastConnection.DEFAULT_RECEIVER_ID)
                .setNamespace("urn:x-cast:com.google.cast.tp.deviceauth")
                .setPayloadType(CastChannel.CastMessage.PayloadType.BINARY)
                .setProtocolVersion(CastChannel.CastMessage.ProtocolVersion.CASTV2_1_0)
                .setSourceId(sourceId)
                .setPayloadBinary(authMessage.toByteString())
                .build()

        sendMessage(message)
        val response = readMessage()
        val authResponse = CastChannel.DeviceAuthMessage.parseFrom(response.payloadBinary)
        if (authResponse.hasError()) {
            throw IOException("Authentication failed: ${authResponse.error.errorType}")
        }

        Log.d(TAG, "Got auth success $authResponse")
    }

    private fun readMessage(): CastChannel.CastMessage {
        val messageLengthBytes = ByteArray(4)
        inputStream.readFully(messageLengthBytes)
        val messageLength = ByteBuffer.wrap(messageLengthBytes).order(ByteOrder.BIG_ENDIAN).int
        val messageBytes = ByteArray(messageLength)
        inputStream.readFully(messageBytes)

        return CastChannel.CastMessage.parseFrom(messageBytes)
    }

    private fun sendMessage(message: CastChannel.CastMessage) {
        if (message.namespace != "urn:x-cast:com.google.cast.tp.heartbeat") {
            //Log.d(TAG, "Sending message $message")
        }
        val messageLength = message.serializedSize
        val messageLengthBytes = ByteBuffer.allocate(4).also {
            it.order(ByteOrder.BIG_ENDIAN)
            it.putInt(messageLength)
        }
        outputStream.write(messageLengthBytes.array())

        message.writeTo(outputStream)
    }

    @SuppressLint("TrustAllX509TrustManager")
    class TrustAllX509TrustManager: X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }
}
