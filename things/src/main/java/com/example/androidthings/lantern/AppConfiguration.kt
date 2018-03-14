package com.example.androidthings.lantern

import android.annotation.SuppressLint
import android.provider.Settings.Secure
import com.example.androidthings.lantern.shared.ChannelConfiguration
import com.example.androidthings.lantern.shared.Direction
import org.json.JSONObject
import android.content.Context
import java.util.*

class AppConfiguration(private val context: Context): Observable() {
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
                    "up": {
                        "type": "space-porthole",
                        "settings": {
                            "latitude": 51.5,
                            "longitude": 0.0
                        }
                    },
                    "forward": {
                        "type": "calendar-clock",
                        "secrets": {
                            "url": "https://calendar.google.com/calendar/ical/nordprojects.co_gjfo1dqt495ll9dt7vmoicjfu4%40group.calendar.google.com/public/basic.ics"
                        }
                    },
                    "down": {
                        "type": "ambient-weather",
                        "settings": {
                            "weatherOverride": "CALM"
                        }
                    }
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

    @SuppressLint("HardwareIds")
    private fun defaultName(): String {
        val hardwareId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        return "Lantern-${hardwareId.takeLast(4)}"
    }
}
