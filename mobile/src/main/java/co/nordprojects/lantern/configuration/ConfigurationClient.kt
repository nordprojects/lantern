package co.nordprojects.lantern.configuration

import android.content.Context
import android.util.Log
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

/**
 * Created by Michael Colville on 29/01/2018.
 */
class ConfigurationClient(val context: Context) {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    val endpoints: MutableList<DiscoveredEndpointInfo> = mutableListOf()
    var activeConnection: ProjectorConfigurationConnection? = null

    companion object {
        val TAG: String = ConfigurationClient::class.java.simpleName
    }

    fun startDiscovery() {
        connectionsClient.startDiscovery(
                "co.nordprojects.lantern.projector",
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.i(TAG, "Start Discovery success") } //TODO
                .addOnFailureListener { Log.i(TAG, "Start Discovery failure") } //TODO
    }

    fun connectTo(endpointId: String) {
        connectionsClient.requestConnection(
                "device name", //TODO
                endpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener { Log.i(TAG, "Start Request Connection success") } //TODO
                .addOnFailureListener { Log.i(TAG, "Start Request Connection failure") } //TODO
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // TODO - add to endpoints, update fragment
            Log.i(TAG, "Endpoint found $endpointId")
            endpoints.add(info)
        }

        override fun onEndpointLost(endpointId: String) {
            // TODO - remove from endpoints, update fragment
            Log.i(TAG, "Endpoint lost $endpointId")
        }
    }

    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // always accept connections
            val transport = ConfigurationConnectionTransport(connectionsClient, endpointId)
            connectionsClient.acceptConnection(endpointId, transport.payloadCallback)

            val connection = ProjectorConfigurationConnection(transport)
            activeConnection = connection
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.isSuccess) {
                activeConnection?.onConnectionAccepted()
            } else {
                activeConnection?.onDisconnected()
                activeConnection = null
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeConnection?.onDisconnected()
            activeConnection = null
        }
    }
}
