package co.nordprojects.lantern.comms

import android.content.Context
import android.os.Handler
import android.util.Log
import co.nordprojects.lantern.App
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

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

    private val retryAdvertisingHandler = Handler()

    fun startAdvertising(name: String) {
        connectionsClient.startAdvertising(
                name,
                SERVICE_ID,
                connectionLifecycleCallback,
                AdvertisingOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.d(TAG, "Began advertising") }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to start advertising. Retrying in two seconds.", e)
                    retryAdvertisingHandler.postDelayed({ startAdvertising(name) }, 2000)
                }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        retryAdvertisingHandler.removeCallbacksAndMessages(null)
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


