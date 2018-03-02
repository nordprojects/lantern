package com.example.androidthings.lantern.configuration

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.androidthings.lantern.shared.ConfigurationConnectionTransport
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.util.*

/**
 * Created by Michael Colville on 29/01/2018.
 */

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING_TO_ENDPOINT,
    CONNECTED
}

class ProjectorClient(val context: Context): Observable() {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    var activeConnection: ProjectorConnection? = null

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
        fun onRequestConnectionFailure()
    }


    fun connectTo(endpointId: String) {
        connectionState = ConnectionState.CONNECTING_TO_ENDPOINT
        Log.i(TAG, "connect to $endpointId")
        connectionsClient.requestConnection(
                Settings.Secure.getString(context.contentResolver, "bluetooth_name"),
                endpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener { Log.i(TAG, "Start Request Connection success") }
                .addOnFailureListener { err ->
                    Log.e(TAG, "Start Request Connection failure", err)
                    connectionState = ConnectionState.DISCONNECTED
                    failureListener?.onRequestConnectionFailure()
                }
    }

    fun disconnect() {
        val endpointID = activeConnection?.endpointId
        if (endpointID != null) {
            connectionsClient.disconnectFromEndpoint(endpointID)
            activeConnection = null
            connectionState = ConnectionState.DISCONNECTED
        }
    }

    private fun connectionDidDisconnect() {
        activeConnection = null
        connectionState = ConnectionState.DISCONNECTED
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
                Log.i(TAG, "On Connection Fail ${resolution.status}")
                connectionDidDisconnect()
                failureListener?.onRequestConnectionFailure()
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "On Disconnected")
            connectionDidDisconnect()
        }
    }
}
