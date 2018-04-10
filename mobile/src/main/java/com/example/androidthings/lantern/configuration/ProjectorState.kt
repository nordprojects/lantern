package com.example.androidthings.lantern.configuration

import com.example.androidthings.lantern.shared.ChannelConfiguration
import com.example.androidthings.lantern.shared.ChannelInfo
import com.example.androidthings.lantern.shared.Direction
import org.json.JSONObject
import java.util.*

/**
 * Contains the current state of the projector including which channel is set to each plane, and
 * which direction the projector is currently facing.
 *
 * Created by Michael Colville on 30/01/2018.
 */

class ProjectorState(json: JSONObject) {
    val planes: Map<Direction, ChannelConfiguration>
    val direction: Direction
    val name: String

    init {
        val planesJson = json.getJSONObject("planes")
        planes = mapOf(
                Direction.UP to ChannelConfiguration(planesJson.getJSONObject("up")),
                Direction.FORWARD to ChannelConfiguration(planesJson.getJSONObject("forward")),
                Direction.DOWN to ChannelConfiguration(planesJson.getJSONObject("down"))
        )
        direction = Direction.withJsonName(json.getString("direction"))
        name = json.getString("name")
    }
}
