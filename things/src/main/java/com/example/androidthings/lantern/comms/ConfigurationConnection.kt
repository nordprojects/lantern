package com.example.androidthings.lantern.comms

import android.util.Log
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.ChannelsRegistry
import com.example.androidthings.lantern.shared.ConfigurationConnectionTransport
import com.example.androidthings.lantern.shared.ConfigurationMessage
import com.example.androidthings.lantern.shared.clone
import org.json.JSONArray
import org.json.JSONObject
import java.util.Observer

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
            ConfigurationMessage.Type.SET_PLANE -> {
                App.instance.config.updatePlane(
                        message.arguments!!.getString("plane"),
                        message.body!!
                )
            }
            ConfigurationMessage.Type.RESET -> {
                App.instance.config.resetToDefaults()
            }
            ConfigurationMessage.Type.LIST_AVAILABLE_CHANNELS -> {
                sendAvailableChannels()
            }
            ConfigurationMessage.Type.SET_NAME -> {
                val newName = message.body!!.getString("name")
                App.instance.config.updateName(newName)
            }
            ConfigurationMessage.Type.ERROR -> {
                Log.e(TAG, "ERROR message received via $this. $message")
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
        val message = ConfigurationMessage(ConfigurationMessage.Type.STATE_UPDATE, body = body)
        transport.sendMessage(message)
    }

    private fun sendAvailableChannels() {
        val body = JSONObject()
        body.put("channels", JSONArray(ChannelsRegistry.channelsInfo.map { it.toJson() }))
        val message = ConfigurationMessage(ConfigurationMessage.Type.AVAILABLE_CHANNELS, body = body)
        transport.sendMessage(message)
    }

    private fun accelerometerUpdated() {
        sendStateUpdate()
    }
    private fun appConfigUpdated() {
        sendStateUpdate()
    }
}