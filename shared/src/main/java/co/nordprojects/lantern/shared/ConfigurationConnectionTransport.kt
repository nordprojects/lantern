package co.nordprojects.lantern.shared

import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.tasks.Task
import org.json.JSONObject

/**
 * Describes a configuration message as sent to/from the projector over Nearby.
 *
 * See appendix of
 * https://docs.google.com/presentation/d/1_CN25gQte2tOFkQx5syV5iguWDHzroUSsXSrBZzaMqY
 * for message format.
 */
data class ConfigurationMessage(val type: Type,
                                val arguments: JSONObject = JSONObject(),
                                val body: JSONObject? = null) {
    enum class Type(val jsonName: String) {
        Error("error"),
        StateUpdate("state-update"),
        AvailableChannels("available-channels"),
        SetPlane("set-plane"),
        ListAvailableChannels("list-available-channels"),
        SetName("set-name"),
        Reset("reset");

        companion object {
            fun withJsonName(jsonName: String): Type {
                val result = Type.values().find { it.jsonName == jsonName }

                if (result == null) {
                    throw IllegalArgumentException("Unknown message type '$jsonName'")
                }

                return result
            }
        }
    }

    constructor(json: JSONObject) : this(
            Type.withJsonName(json.getString("type")),
            json.clone().also {
                it.remove("type")
                it.remove("body")
            },
            json.optJSONObject("body")
    )

    constructor(jsonBytes: ByteArray) : this(JSONObject(String(jsonBytes)))

    fun toJson(): JSONObject {
        val json = arguments.clone()
        json.put("type", type.jsonName)
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
        MessageDecodeError,
        UnexpectedErrorWhileHandlingMessage
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

            try {
                onMessageReceived?.invoke(message)
            }
            catch (e: Exception) {
                Log.e(TAG, "Error while handling message $message", e)
                sendErrorMessage(ResponseErrorType.UnexpectedErrorWhileHandlingMessage)
                return
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }
    }

    fun sendMessage(message: ConfigurationMessage): Task<Void> {
        val messageBytes = message.toJsonBytes()
        return client.sendPayload(endpointId, Payload.fromBytes(messageBytes))
    }
    fun sendErrorMessage(errorType: ResponseErrorType): Task<Void> {
        val errorMessage = ConfigurationMessage(
                ConfigurationMessage.Type.Error,
                body = JSONObject(mapOf(
                        "type" to errorType.name
                ))
        )
        return sendMessage(errorMessage)
    }
}
