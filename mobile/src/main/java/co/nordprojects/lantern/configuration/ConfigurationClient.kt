package co.nordprojects.lantern.configuration

import android.content.Context
import android.util.Log
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

/**
 * Created by Michael Colville on 29/01/2018.
 */

data class Endpoint(val id: String, val info: DiscoveredEndpointInfo)

enum class ConnectionState {
    UNKNOWN,
    LOOKING_FOR_ENDPOINTS,
    ENDPOINTS_AVAILABLE,
    CONNECTING_TO_ENDPOINT,
    CONNECTED
}

class ConfigurationClient(val context: Context) {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    val endpoints: ArrayList<Endpoint> = arrayListOf()
    var activeConnection: ProjectorConnection? = null
    var listener: ConfigurationClientUpdatedListener? = null
    var endpointsUpdatedListener: EndpointsUpdatedListener? = null
    var connectionState: ConnectionState = ConnectionState.UNKNOWN
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != value) {
                listener?.onConfigurationClientUpdated()
            }
        }

    companion object {
        val TAG: String = ConfigurationClient::class.java.simpleName
    }

    interface EndpointsUpdatedListener {
        fun onEndpointsUpdated()
    }

    interface ConfigurationClientUpdatedListener {
        fun onConfigurationClientUpdated()
    }

    fun startDiscovery() {
        connectionState = ConnectionState.LOOKING_FOR_ENDPOINTS
        connectionsClient.startDiscovery(
                "co.nordprojects.lantern.projector",
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.i(TAG, "Start Discovery success") } //TODO
                .addOnFailureListener { Log.i(TAG, "Start Discovery failure") } //TODO
    }

    fun connectTo(endpointId: String) {
        connectionState = ConnectionState.CONNECTING_TO_ENDPOINT
        Log.i(TAG, "connect to $endpointId")
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
            endpoints.add(Endpoint(endpointId, info))
            connectionState = ConnectionState.ENDPOINTS_AVAILABLE
            endpointsUpdatedListener?.onEndpointsUpdated()
        }

        override fun onEndpointLost(endpointId: String) {
            // TODO - remove from endpoints, update fragment
            Log.i(TAG, "Endpoint lost $endpointId")
            val endpoint = endpoints.find { it.id == endpointId }
            endpoints.remove(endpoint)

            if (endpoints.size == 0) {
                connectionState = ConnectionState.LOOKING_FOR_ENDPOINTS
            }
            endpointsUpdatedListener?.onEndpointsUpdated()
        }
    }

    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            // always accept connections
            val transport = ConfigurationConnectionTransport(connectionsClient, endpointId)
            connectionsClient.acceptConnection(endpointId, transport.payloadCallback)

            val connection = ProjectorConnection(transport)
            activeConnection = connection
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            if (resolution.status.isSuccess) {
                activeConnection?.onConnectionAccepted()
                connectionState = ConnectionState.CONNECTED
            } else {
                activeConnection?.onDisconnected()
                activeConnection = null
            }
        }

        override fun onDisconnected(endpointId: String) {
            // TODO - decide what to do here. Show warning on HomeActivity or go back to SearchActivity?
            activeConnection?.onDisconnected()
            activeConnection = null
        }
    }
}
