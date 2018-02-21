package co.nordprojects.lantern.configuration

import co.nordprojects.lantern.shared.ChannelConfiguration
import co.nordprojects.lantern.shared.ChannelInfo
import co.nordprojects.lantern.shared.Direction
import org.json.JSONObject
import java.util.*

/**
 * Created by Michael Colville on 30/01/2018.
 */

class ProjectorState(json: JSONObject) : Observable() {
    private val _planes = mutableMapOf<Direction, ChannelConfiguration>(
            Direction.UP to ChannelConfiguration.blank,
            Direction.FORWARD to ChannelConfiguration.blank,
            Direction.DOWN to ChannelConfiguration.blank
    )
    val planes: Map<Direction, ChannelConfiguration>
        get() = _planes
    var direction: Direction = Direction.FORWARD
    var availableChannels: List<ChannelInfo> = listOf()
    var name: String = ""

    init {
        updateWithJSON(json)
    }

    fun updateWithJSON(json: JSONObject) {
        val planesJson = json.getJSONObject("planes")

        updatePlane("up", planesJson.getJSONObject("up"))
        updatePlane("forward", planesJson.getJSONObject("forward"))
        updatePlane("down", planesJson.getJSONObject("down"))

        direction = Direction.withJsonName(json.getString("direction"))
        name = json.getString("name")

        setChanged()
        notifyObservers()
    }

    private fun updatePlane(jsonDirection: String, jsonConfig: JSONObject) {
        val direction = Direction.withJsonName(jsonDirection)
        _planes[direction] = ChannelConfiguration(jsonConfig)
    }

    fun updateAvailableChannelsWithJSON(messageBody: JSONObject) {
        val channelsInfoJson = messageBody.getJSONArray("channels")
        val channelsInfo = arrayListOf<ChannelInfo>()

        for (i in 0 until channelsInfoJson.length()) {
            val channelInfoJson = channelsInfoJson.getJSONObject(i)
            val channelInfo = ChannelInfo(channelInfoJson)
            channelsInfo.add(channelInfo)
        }

        availableChannels = channelsInfo

        setChanged()
        notifyObservers()
    }

    fun channelInfoForChannelType(type: String): ChannelInfo? {
        return availableChannels.find { it.id == type }
    }


}
