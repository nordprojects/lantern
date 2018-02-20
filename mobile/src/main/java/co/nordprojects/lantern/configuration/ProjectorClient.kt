package co.nordprojects.lantern.configuration

import android.content.Context
import android.provider.Settings
import android.util.Log
import co.nordprojects.lantern.shared.ConfigurationConnectionTransport
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.util.*

/**
 * Created by Michael Colville on 29/01/2018.
 */

data class Endpoint(val id: String, val info: DiscoveredEndpointInfo)

enum class DiscoveryState {
    UNINITIALISED,
    LOOKING_FOR_ENDPOINTS,
    ENDPOINTS_AVAILABLE
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING_TO_ENDPOINT,
    CONNECTED
}

class ProjectorClient(val context: Context): Observable() {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    val endpoints: ArrayList<Endpoint> = arrayListOf()
    private var activeEndpointID: String? = null
    var activeConnection: ProjectorConnection? = null

    var discoveryState: DiscoveryState = DiscoveryState.UNINITIALISED
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field = value
                setChanged()
                notifyObservers()
            }
        }
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field = value
                setChanged()
                notifyObservers()
            }
        }
    var failureListener: ProjectorClientFailureListener? = null

    companion object {
        private val TAG: String = ProjectorClient::class.java.simpleName
    }

    interface ProjectorClientFailureListener {
        fun onStartDiscoveryFailure()
        fun onRequestConnectionFailure()
    }

    fun startDiscovery() {
        Log.i(TAG, "START DISCOVERY")
        discoveryState = DiscoveryState.LOOKING_FOR_ENDPOINTS
        connectionsClient.startDiscovery(
                "co.nordprojects.lantern.projector",
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.i(TAG, "Start Discovery success") }
                .addOnFailureListener {
                    Log.i(TAG, "Start Discovery failure")
                    discoveryState = DiscoveryState.UNINITIALISED
                    failureListener?.onStartDiscoveryFailure()
                }
    }

    fun connectTo(endpointId: String) {
        connectionState = ConnectionState.CONNECTING_TO_ENDPOINT
        activeEndpointID = endpointId
        Log.i(TAG, "connect to $endpointId")
        connectionsClient.requestConnection(
                Settings.Secure.getString(context.contentResolver, "bluetooth_name"),
                endpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener { Log.i(TAG, "Start Request Connection success") }
                .addOnFailureListener {
                    Log.i(TAG, "Start Request Connection failure")
                    discoveryState = DiscoveryState.ENDPOINTS_AVAILABLE
                    failureListener?.onRequestConnectionFailure()
                }
    }

    fun disconnect() {
        val endpointID = activeEndpointID
        if (endpointID != null) {
            connectionsClient.disconnectFromEndpoint(endpointID)
            connectionState = ConnectionState.DISCONNECTED
        }
    }

    private fun connectionDidDisconnect() {
        connectionState = ConnectionState.DISCONNECTED
        activeConnection?.onDisconnected()
        activeConnection = null
        // TODO - restart nearby discovering? remove all existing endpoints
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "Endpoint found $endpointId")
            endpoints.add(Endpoint(endpointId, info))
            setChanged()
            notifyObservers()
            discoveryState = DiscoveryState.ENDPOINTS_AVAILABLE
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "Endpoint lost $endpointId")
            val endpoint = endpoints.find { it.id == endpointId }
            endpoints.remove(endpoint)
            setChanged()
            notifyObservers()
            if (endpoints.size == 0) {
                discoveryState = DiscoveryState.LOOKING_FOR_ENDPOINTS
            }
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
                Log.i(TAG, "On Connection Fail")
                connectionDidDisconnect()
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "On Disconnected")
            connectionDidDisconnect()
        }
    }
}
