package co.nordprojects.lantern

import android.provider.Settings.Secure
import co.nordprojects.lantern.shared.ChannelConfiguration
import co.nordprojects.lantern.shared.Direction
import org.json.JSONObject
import java.util.*

class AppConfiguration: Observable() {
    private val _planes = mutableMapOf<Direction, ChannelConfiguration>(
            Direction.UP to ChannelConfiguration.blank,
            Direction.FORWARD to ChannelConfiguration.blank,
            Direction.DOWN to ChannelConfiguration.blank
    )
    var name: String = defaultName()
        private set
    val planes: Map<Direction, ChannelConfiguration>
        get() = _planes

    fun updatePlane(jsonDirection: String, jsonConfig: JSONObject, skipNotify: Boolean = false) {
        val direction = Direction.withJsonName(jsonDirection)

        _planes[direction] = ChannelConfiguration(jsonConfig)

        setChanged()
        if (!skipNotify) {
            notifyObservers()
        }
    }

    fun updateName(name: String, skipNotify: Boolean = false) {
        this.name = name

        setChanged()
        if (!skipNotify) {
            notifyObservers()
        }
    }

    fun resetToDefaults() {
        val defaultConfig = """
            {
                "planes": {
                    "up": {"type": "blank"},
                    "forward": {"type": "lamp"},
                    "down": {"type": "now-playing"}
                }
            }
            """
        updateWithJson(JSONObject(defaultConfig))
    }

    fun updateWithJson(json: JSONObject) {
        updateName(json.optString("name", defaultName()), skipNotify = true)

        val planesJson = json.getJSONObject("planes")
        updatePlane("up", planesJson.getJSONObject("up"), skipNotify = true)
        updatePlane("forward", planesJson.getJSONObject("forward"), skipNotify = true)
        updatePlane("down", planesJson.getJSONObject("down"), skipNotify = true)

        notifyObservers()
    }

    fun toJson(includingSecrets: Boolean = false): JSONObject {
        return JSONObject(mapOf(
                "name" to name,
                "planes" to mapOf(
                        "up" to planes[Direction.UP]?.toJson(includingSecrets = includingSecrets),
                        "forward" to planes[Direction.FORWARD]?.toJson(includingSecrets = includingSecrets),
                        "down" to planes[Direction.DOWN]?.toJson(includingSecrets = includingSecrets)
                )
        ))
    }

    private fun defaultName(): String {
        val hardwareId = Secure.getString(App.instance.contentResolver, Secure.ANDROID_ID)
        return "Lantern-${hardwareId.takeLast(4)}"
    }
}
