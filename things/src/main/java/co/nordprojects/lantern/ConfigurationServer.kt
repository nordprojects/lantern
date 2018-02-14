package co.nordprojects.lantern

import android.content.Context
import android.os.Handler
import android.util.Log
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import co.nordprojects.lantern.shared.ConfigurationMessage
import co.nordprojects.lantern.shared.clone
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Task
import org.json.JSONArray
import org.json.JSONObject
import java.util.Observer

/**
 * Server advertises for Nearby connections, and creates ConfigurationConnection objects
 * to handle individual clients.
 *
 * Created by joerick on 24/01/18.
 */
class ConfigurationServer(val context: Context) {
    private val TAG = ConfigurationServer::class.java.simpleName

    private val SERVICE_ID = "co.nordprojects.lantern.projector"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val activeConnections: MutableMap<String, ConfigurationConnection> = mutableMapOf()

    fun startAdvertising() {
        connectionsClient.startAdvertising(
                App.instance.config.name,
                SERVICE_ID,
                connectionLifecycleCallback,
                AdvertisingOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.d(TAG, "Began advertising") }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to start advertising. Retrying in two seconds.", e)
                    Handler().postDelayed({ startAdvertising() }, 2000)
                }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // always accept connections
            val transport = ConfigurationConnectionTransport(connectionsClient, endpointId)
            connectionsClient.acceptConnection(endpointId, transport.payloadCallback)

            val connection = ConfigurationConnection(transport)
            activeConnections[endpointId] = connection
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.isSuccess) {
                activeConnections[endpointId]?.onConnectionAccepted()
            } else {
                activeConnections[endpointId]?.onDisconnected()
                activeConnections.remove(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnections[endpointId]?.onDisconnected()
            activeConnections.remove(endpointId)
        }
    }
}


class ConfigurationConnection(val transport: ConfigurationConnectionTransport) {
    private val TAG = ConfigurationConnection::class.java.simpleName
    private val accelerometerObserver = Observer { _, _ -> accelerometerUpdated() }
    private val appConfigObserver = Observer { _, _ -> appConfigUpdated() }

    init {
        transport.onMessageReceived = { m -> onMessageReceived(m) }
    }

    fun onConnectionAccepted() {
        Log.i(TAG, "Connected to ${transport.endpointId}")
        sendStateUpdate()
        sendAvailableChannels()
        App.instance.accelerometer.addObserver(accelerometerObserver)
        App.instance.config.addObserver(appConfigObserver)
    }
    fun onMessageReceived(message: ConfigurationMessage) {
        Log.d(TAG, "Received message from ${transport.endpointId}. $message")

        when (message.type) {
            ConfigurationMessage.Type.SetPlane -> {
                App.instance.config.updatePlane(
                        message.arguments.getString("plane"),
                        message.body!!
                )
            }
            ConfigurationMessage.Type.Reset -> {
                App.instance.config.resetToDefaults()
            }
            ConfigurationMessage.Type.ListAvailableChannels -> {
                sendAvailableChannels()
            }
            ConfigurationMessage.Type.SetName -> {
                val newName = message.body!!.getString("name")
                App.instance.config.updateName(newName)
            }
            ConfigurationMessage.Type.Error -> {
                Log.e(TAG, "Error message received via $this. $message")
            }
            else -> { throw IllegalArgumentException("Can't handle message type ${message.type}") }
        }
    }
    fun onDisconnected() {
        Log.i(TAG, "Disconnected from ${transport.endpointId}")
        App.instance.accelerometer.deleteObserver(accelerometerObserver)
        App.instance.config.deleteObserver(appConfigObserver)
    }

    private fun sendStateUpdate() {
        val body = App.instance.config.toJson(includingSecrets = false).clone()
        body.put("direction", App.instance.accelerometer.direction?.jsonName)
        val message = ConfigurationMessage(ConfigurationMessage.Type.StateUpdate, body = body)
        transport.sendMessage(message)
    }

    private fun sendAvailableChannels() {
        val body = JSONObject()
        body.put("channels", JSONArray(ChannelsRegistry.channelsInfo.map { it.toJson() }))
        val message = ConfigurationMessage(ConfigurationMessage.Type.AvailableChannels, body = body)
        transport.sendMessage(message)
    }

    private fun accelerometerUpdated() {
        sendStateUpdate()
    }
    private fun appConfigUpdated() {
        sendStateUpdate()
    }
}
