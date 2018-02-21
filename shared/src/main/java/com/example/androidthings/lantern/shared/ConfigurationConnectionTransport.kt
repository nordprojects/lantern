package com.example.androidthings.lantern.shared

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
                                val arguments: JSONObject? = null,
                                val body: JSONObject? = null) {
    enum class Type {
        ERROR,
        STATE_UPDATE, AVAILABLE_CHANNELS,
        SET_PLANE, LIST_AVAILABLE_CHANNELS, SET_NAME, RESET;

        val jsonName: String get() {
            return when (this) {
                ERROR -> "error"
                STATE_UPDATE -> "state-update"
                AVAILABLE_CHANNELS -> "available-channels"
                SET_PLANE -> "set-plane"
                LIST_AVAILABLE_CHANNELS -> "list-available-channels"
                SET_NAME -> "set-name"
                RESET -> "reset"
            }
        }

        companion object {
            fun withJsonName(jsonName: String): Type {
                return values().find { it.jsonName == jsonName } ?:
                        throw IllegalArgumentException("Unknown message type '$jsonName'")
            }
        }
    }

    constructor(json: JSONObject) : this(
            Type.withJsonName(json.getString("type")),
            json.optJSONObject("arguments"),
            json.optJSONObject("body")
    )

    constructor(jsonBytes: ByteArray) : this(JSONObject(String(jsonBytes)))

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("type", type.jsonName)
        json.put("arguments", arguments)
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
        BAD_PAYLOAD_TYPE,
        MESSAGE_DECODE_ERROR,
        UNEXPECTED_ERROR_WHILE_HANDLING_MESSAGE
    }
    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) {
                Log.e(TAG, "Bad payload type. Payload discarded.")
                sendErrorMessage(ResponseErrorType.BAD_PAYLOAD_TYPE)
                return
            }

            val message: ConfigurationMessage
            try {
                message = ConfigurationMessage(payload.asBytes()!!)
            }
            catch (e: Exception) {
                Log.e(TAG, "Failed to decode message", e)
                sendErrorMessage(ResponseErrorType.MESSAGE_DECODE_ERROR)
                return
            }

            try {
                onMessageReceived?.invoke(message)
            }
            catch (e: Exception) {
                Log.e(TAG, "Error while handling message $message", e)
                sendErrorMessage(ResponseErrorType.UNEXPECTED_ERROR_WHILE_HANDLING_MESSAGE)
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
                ConfigurationMessage.Type.ERROR,
                body = JSONObject(mapOf(
                        "type" to errorType.name
                ))
        )
        return sendMessage(errorMessage)
    }
}
