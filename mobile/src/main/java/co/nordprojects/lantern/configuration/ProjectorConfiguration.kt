package co.nordprojects.lantern.configuration

import co.nordprojects.lantern.shared.ChannelConfiguration
import co.nordprojects.lantern.shared.Direction
import org.json.JSONObject
import java.util.*

/**
 * Created by Michael Colville on 30/01/2018.
 */

class ProjectorConfiguration: Observable() {

    private val _planes = mutableMapOf<Direction, ChannelConfiguration>(
            Direction.UP to ChannelConfiguration.blank,
            Direction.FORWARD to ChannelConfiguration.blank,
            Direction.DOWN to ChannelConfiguration.blank
    )
    val planes: Map<Direction, ChannelConfiguration>
        get() = _planes

    var direction: Direction = Direction.FORWARD

    fun updateWithJSON(json: JSONObject) {
        val planesJson = json.getJSONObject("planes")

        updatePlane("up", planesJson.getJSONObject("up"))
        updatePlane("forward", planesJson.getJSONObject("forward"))
        updatePlane("down", planesJson.getJSONObject("down"))

        direction = Direction.withJsonName(json.getString("direction"))

        setChanged()
        notifyObservers()
    }

    fun updatePlane(jsonDirection: String, jsonConfig: JSONObject) {
        val direction = Direction.withJsonName(jsonDirection)
        _planes[direction] = ChannelConfiguration(jsonConfig)
    }


}
