package co.nordprojects.lantern.configuration

import android.util.Log
import co.nordprojects.lantern.shared.*
import org.json.JSONObject

/**
 * Created by Michael Colville on 30/01/2018.
 */
class ProjectorConnection(val transport: ConfigurationConnectionTransport) {

    var projectorState: ProjectorState? = null
    val endpointId: String
        get() = transport.endpointId
    companion object {
        val TAG: String = ProjectorConnection::class.java.simpleName
    }

    init {
        transport.onMessageReceived = { m -> onMessageReceived(m) }
    }

    fun onConnectionAccepted() {
        Log.i(TAG, "Connected to ${transport.endpointId}")
    }

    private fun onMessageReceived(message: ConfigurationMessage) {
        Log.d(TAG, "Received message from ${transport.endpointId}. $message")

        when (message.type) {
            ConfigurationMessage.Type.STATE_UPDATE -> {
                if (projectorState == null) {
                    projectorState = ProjectorState(message.body!!)
                } else {
                    projectorState?.updateWithJSON(message.body!!)
                }
            }
            ConfigurationMessage.Type.AVAILABLE_CHANNELS -> {
                projectorState?.updateAvailableChannelsWithJSON(message.body!!)
            }
            ConfigurationMessage.Type.ERROR -> {
                Log.e(TAG, "Error message received from $this. $message")
            }
            else -> { throw IllegalArgumentException("Can't handle message type ${message.type}") }
        }
    }

    fun sendSetPlane(direction: Direction, configuration: ChannelConfiguration) {
        val body = configuration.toJson(includingSecrets = true)
        val arguments = JSONObject().apply {
            put("plane", direction.jsonName)
        }
        val message = ConfigurationMessage(ConfigurationMessage.Type.SET_PLANE, arguments, body)
        transport.sendMessage(message)
    }

    fun sendResetDevice() {
        val message = ConfigurationMessage(ConfigurationMessage.Type.RESET)
        transport.sendMessage(message)
    }

    fun sendSetName(name: String) {
        val message = ConfigurationMessage(
                ConfigurationMessage.Type.SET_NAME,
                body = JSONObject(mapOf("name" to name))
        )
        transport.sendMessage(message)
    }
}
