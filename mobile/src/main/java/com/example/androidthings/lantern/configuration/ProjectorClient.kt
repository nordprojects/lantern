package com.example.androidthings.lantern.configuration

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.androidthings.lantern.shared.ConfigurationConnectionTransport
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.util.*

/**
 * Sets up the connection to a projector given an Endpoint ID from Discovery.
 *
 * Created by Michael Colville on 29/01/2018.
 */

class ProjectorClient(val context: Context): Observable() {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    var activeConnection: ProjectorConnection? = null
    var lastEndpoint: String? = null

    companion object {
        private val TAG: String = ProjectorClient::class.java.simpleName
    }

    fun connectTo(endpointId: String, success: () -> Unit, failure: () -> Unit) {
        if (activeConnection != null) {
            disconnect()
        }
        lastEndpoint = endpointId
        connectionsClient.requestConnection(
            Settings.Secure.getString(context.contentResolver, "bluetooth_name"),
            endpointId,
            object: ConnectionLifecycleCallback() {
                override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                    val transport = ConfigurationConnectionTransport(connectionsClient, endpointId)
                    connectionsClient.acceptConnection(endpointId, transport.payloadCallback)
                    activeConnection = ProjectorConnection(transport)
                }

                override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
                    if (resolution.status.isSuccess) {
                        success()
                        setChanged()
                        notifyObservers()
                    } else {
                        Log.i(TAG, "On Connection Fail ${resolution.status}")
                        connectionDidDisconnect()
                        failure()
                    }
                }

                override fun onDisconnected(endpointId: String) {
                    Log.i(TAG, "On Disconnected")
                    connectionDidDisconnect()
                }
            })
            .addOnSuccessListener { Log.i(TAG, "Start Request Connection success") }
            .addOnFailureListener { err ->
                Log.e(TAG, "Start Request Connection failure", err)
                failure()
            }
    }

    private fun disconnect() {
        val endpointID = activeConnection?.endpointId
        if (endpointID != null) {
            connectionsClient.disconnectFromEndpoint(endpointID)
            activeConnection = null
        }
    }

    private fun connectionDidDisconnect() {
        activeConnection = null
        setChanged()
        notifyObservers()
    }
}
