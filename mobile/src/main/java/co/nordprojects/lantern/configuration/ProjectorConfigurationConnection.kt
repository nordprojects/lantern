package co.nordprojects.lantern.configuration

import android.util.Log
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import co.nordprojects.lantern.shared.ConfigurationMessage

/**
 * Created by Michael Colville on 30/01/2018.
 */
class ProjectorConfigurationConnection(val transport: ConfigurationConnectionTransport) {

    var projectorConfig: ProjectorConfiguration = ProjectorConfiguration()
    companion object {
        val TAG: String = ProjectorConfigurationConnection::class.java.simpleName
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
            ConfigurationMessage.Type.StateUpdate -> {
                projectorConfig.updateWithJSON(message.body!!)
            }
            ConfigurationMessage.Type.AvailableChannels -> {
                //TODO - save for channels activity
            }
            else -> { throw IllegalArgumentException("Can't handle message type ${message.type}") }
        }
    }

    fun onDisconnected() {
        Log.i(TAG, "Disconnected from ${transport.endpointId}")

        // TODO - either reconnect or destroy projector config
        // OR may not be needed as this object is destroyed on disconnect
    }
}
