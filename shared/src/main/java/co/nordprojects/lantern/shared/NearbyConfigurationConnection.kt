package co.nordprojects.lantern.shared

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.tasks.Task
import org.json.JSONObject


data class ConfigurationMessage(val type: String,
                                val arguments: JSONObject? = null,
                                val body: JSONObject? = null) {

    constructor(json: JSONObject) : this(
            json.getString("type"),
            json.clone().also {
                it.remove("type")
                it.remove("body")
            },
            json.optJSONObject("body")
    )

    constructor(jsonBytes: ByteArray) : this(JSONObject(String(jsonBytes)))

    fun toJson(): JSONObject {
        val json = arguments?.clone() ?: JSONObject()
        json.put("type", type)
        json.put("body", body)
        return json
    }
    fun toJsonBytes(): ByteArray {
        return toJson().toString().toByteArray()
    }
}


class ConfigurationConnectionTransport(val client: ConnectionsClient,
                                       val endpointId: String) {
    private val TAG = ConfigurationConnectionTransport::class.java.simpleName
    var onMessageReceived: ((ConfigurationMessage) -> Unit)? = null

    enum class ResponseErrorType {
        BadPayloadType,
        MessageDecodeError
    }
    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) {
                Log.e(TAG, "Bad payload type. Payload discarded.")
                sendErrorMessage(ResponseErrorType.BadPayloadType)
                return
            }

            val message: ConfigurationMessage
            try {
                message = ConfigurationMessage(payload.asBytes()!!)
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to decode message", e)
                sendErrorMessage(ResponseErrorType.MessageDecodeError)
                return
            }

            onMessageReceived?.invoke(message)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    fun sendMessage(message: ConfigurationMessage): Task<Void> {
        val messageBytes = message.toJsonBytes()
        return client.sendPayload(endpointId, Payload.fromBytes(messageBytes))
    }
    fun sendErrorMessage(errorType: ResponseErrorType): Task<Void> {
        val errorMessage = ConfigurationMessage("error", null,
                JSONObject(mapOf("type" to errorType.name))
        )
        return sendMessage(errorMessage)
    }
}
