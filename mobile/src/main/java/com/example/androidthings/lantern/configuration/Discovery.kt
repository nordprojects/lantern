package com.example.androidthings.lantern.configuration

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Strategy
import java.util.*

/**
 * Created by Michael Colville on 02/03/2018.
 */


data class Endpoint(val id: String, val info: DiscoveredEndpointInfo)

enum class DiscoveryState {
    UNINITIALISED,
    LOOKING_FOR_ENDPOINTS,
    ENDPOINTS_AVAILABLE
}


class Discovery(val context: Context): Observable() {

    companion object {
        private val TAG: String = Discovery::class.java.simpleName
    }

    // TODO - this needs to be shared with ProjectorClient
    private val connectionsClient = Nearby.getConnectionsClient(context)
    val endpoints: ArrayList<Endpoint> = arrayListOf()
    var failureListener: DiscoveryFailureListener? = null


    var discoveryState: DiscoveryState = DiscoveryState.UNINITIALISED
        set(value) {
            val oldValue = field
            if (oldValue != value) {
                field = value
                setChanged()
                notifyObservers()
            }
        }

    interface DiscoveryFailureListener {
        fun onStartDiscoveryFailure()
    }

    fun startDiscovery() {
        Log.i(TAG, "START DISCOVERY")
        discoveryState = DiscoveryState.LOOKING_FOR_ENDPOINTS
        connectionsClient.startDiscovery(
                "com.example.androidthings.lantern.projector",
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.i(TAG, "Start Discovery success") }
                .addOnFailureListener { err ->
                    Log.e(TAG, "Start Discovery failure", err)
                    discoveryState = DiscoveryState.UNINITIALISED
                    failureListener?.onStartDiscoveryFailure()
                }
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
}