package com.example.androidthings.lantern.configuration

import android.util.Log
import com.example.androidthings.lantern.shared.*
import org.json.JSONObject
import java.util.*

/**
 * Created by Michael Colville on 30/01/2018.
 */

class ProjectorConnection(private val transport: ConfigurationConnectionTransport): Observable() {

    var projectorState: ProjectorState? = null
    var availableChannels: List<ChannelInfo> = listOf()
    val endpointId: String
        get() = transport.endpointId
    companion object {
        val TAG: String = ProjectorConnection::class.java.simpleName
    }

    init {
        transport.onMessageReceived = { m -> onMessageReceived(m) }
    }

    private fun onMessageReceived(message: ConfigurationMessage) {
        Log.d(TAG, "Received message from ${transport.endpointId}. $message")

        when (message.type) {
            ConfigurationMessage.Type.STATE_UPDATE -> {
                projectorState = ProjectorState(message.body!!)
                setChanged()
                notifyObservers()
            }
            ConfigurationMessage.Type.AVAILABLE_CHANNELS -> {
                updateAvailableChannelsWithJSON(message.body!!)
                setChanged()
                notifyObservers()
            }
            ConfigurationMessage.Type.ERROR -> {
                Log.e(TAG, "Error message received from $this. $message")
            }
            else -> { throw IllegalArgumentException("Can't handle message type ${message.type}") }
        }
    }

    private fun updateAvailableChannelsWithJSON(messageBody: JSONObject) {
        val channelsInfoJson = messageBody.getJSONArray("channels")
        val channelsInfo = arrayListOf<ChannelInfo>()
        for (i in 0 until channelsInfoJson.length()) {
            val channelInfoJson = channelsInfoJson.getJSONObject(i)
            val channelInfo = ChannelInfo(channelInfoJson)
            channelsInfo.add(channelInfo)
        }
        availableChannels = channelsInfo
    }

    fun channelInfoForChannelType(type: String): ChannelInfo? {
        return availableChannels.find { it.id == type }
    }

    fun sendSetPlane(direction: Direction, configuration: ChannelConfiguration) {
        val body = configuration.toJson(includingSecrets = true)
        val arguments = JSONObject().apply {
            put("plane", direction.jsonName)
        }
        val message = ConfigurationMessage(ConfigurationMessage.Type.SET_PLANE, arguments, body)
        transport.sendMessage(message)
    }

    fun sendResetDevice() {
        val message = ConfigurationMessage(ConfigurationMessage.Type.RESET)
        transport.sendMessage(message)
    }

    fun sendSetName(name: String) {
        val message = ConfigurationMessage(
                ConfigurationMessage.Type.SET_NAME,
                body = JSONObject(mapOf("name" to name))
        )
        transport.sendMessage(message)
    }
}
