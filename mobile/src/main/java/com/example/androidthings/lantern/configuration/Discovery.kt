package com.example.androidthings.lantern.configuration

import android.content.Context
import android.os.Handler
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


class Discovery(val context: Context): Observable() {

    data class Endpoint(val id: String, val info: DiscoveredEndpointInfo)

    class TimeoutException: Exception()

    companion object {
        private val TAG: String = Discovery::class.java.simpleName
    }

    private val connectionsClient = Nearby.getConnectionsClient(context)
    val endpoints: ArrayList<Endpoint> = arrayListOf()
    private val timeoutHandler = Handler()

    fun startDiscovery(failure: (error: Exception) -> Unit) {
        timeoutHandler.removeCallbacksAndMessages(null)

        connectionsClient.startDiscovery(
                "com.example.androidthings.lantern.projector",
                endpointDiscoveryCallback,
                DiscoveryOptions(Strategy.P2P_CLUSTER))
                .addOnSuccessListener { Log.i(TAG, "Start Discovery success") }
                .addOnFailureListener { error ->
                    Log.e(TAG, "Start Discovery failure", error)
                    timeoutHandler.removeCallbacksAndMessages(null)
                    failure(error)
                }

        timeoutHandler.postDelayed({
            if (endpoints.size == 0) {
                failure(TimeoutException())
                stopDiscovery()
            }
        }, 30 * 1000)
    }

    fun stopDiscovery() {
        timeoutHandler.removeCallbacksAndMessages(null)
        connectionsClient.stopDiscovery()
        endpoints.clear()
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "Endpoint found $endpointId")
            endpoints.add(Endpoint(endpointId, info))
            setChanged()
            notifyObservers()
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "Endpoint lost $endpointId")
            val endpoint = endpoints.find { it.id == endpointId }
            endpoints.remove(endpoint)
            setChanged()
            notifyObservers()
        }
    }
}