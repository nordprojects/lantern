package co.nordprojects.lantern

import android.content.Context
import android.os.Handler
import android.util.Log
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import co.nordprojects.lantern.shared.ConfigurationMessage
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Task
import org.json.JSONObject

/**
 * Server advertises for Nearby connections, and creates ConfigurationConnection objects
 * to handle individual clients.
 *
 * Created by joerick on 24/01/18.
 */
class ConfigurationServer(val context: Context) {
    private val TAG = ConfigurationServer::class.java.simpleName
    private val DEVICE_NAME = "Projector Pi"
    private val SERVICE_ID = "co.nordprojects.lantern.projector"
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val activeConnections: MutableMap<String, ConfigurationConnection> = mutableMapOf()

    fun startAdvertising() {
        connectionsClient.startAdvertising(DEVICE_NAME,
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

    init {
        transport.onMessageReceived = { m -> onMessageReceived(m) }
    }

    fun onConnectionAccepted() {
        Log.i(TAG, "Connected to ${transport.endpointId}")
    }

    fun onMessageReceived(message: ConfigurationMessage) {
        Log.d(TAG, "Received message from ${transport.endpointId}. ${message}")
    }

    fun onDisconnected() {
        Log.i(TAG, "Disconnected from ${transport.endpointId}")
    }
}
