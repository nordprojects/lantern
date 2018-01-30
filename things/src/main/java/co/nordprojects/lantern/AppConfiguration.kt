package co.nordprojects.lantern

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import co.nordprojects.lantern.shared.Direction
import co.nordprojects.lantern.shared.clone
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.util.*


class AppConfiguration: Observable() {
    private val _planes = mutableMapOf<Direction, ChannelConfiguration>(
            Direction.UP to ChannelConfiguration.blank,
            Direction.FORWARD to ChannelConfiguration.blank,
            Direction.DOWN to ChannelConfiguration.blank
    )
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

    fun resetToDefaults() {
        val defaultConfig = """
            {
                "planes": {
                    "up": {"type": "blank"},
                    "forward": {"type": "calendar-clock"},
                    "down": {"type": "now-playing"}
                }
            }
            """
        updateWithJson(JSONObject(defaultConfig))
    }

    fun updateWithJson(json: JSONObject) {
        val planesJson = json.getJSONObject("planes")

        updatePlane("up", planesJson.getJSONObject("up"), skipNotify = true)
        updatePlane("forward", planesJson.getJSONObject("forward"), skipNotify = true)
        updatePlane("down", planesJson.getJSONObject("down"), skipNotify = true)

        notifyObservers()
    }

    fun toJson(includingSecrets: Boolean = false): JSONObject {
        return JSONObject(mapOf(
                "planes" to mapOf(
                        "up" to planes[Direction.UP]?.toJson(includingSecrets = includingSecrets),
                        "forward" to planes[Direction.FORWARD]?.toJson(includingSecrets = includingSecrets),
                        "down" to planes[Direction.DOWN]?.toJson(includingSecrets = includingSecrets)
                )
        ))
    }
}

@SuppressLint("ParcelCreator")
@Parcelize
class ChannelConfiguration(val type: String,
                           val settings: JSONObject,
                           val secret: JSONObject?) : Parcelable {
    constructor(json: JSONObject) : this(
            json.getString("type"),
            json.clone().also {
                it.remove("secret")
            },
            json.optJSONObject("secret")
    )

    fun toJson(includingSecrets: Boolean = false): JSONObject {
        val json = settings.clone()
        json.put("type", type)
        if (includingSecrets) {
            json.put("secret", secret)
        }
        return json
    }

    companion object : Parceler<ChannelConfiguration> {
        val blank: ChannelConfiguration
            get() = ChannelConfiguration(JSONObject("""{"type": "blank"}"""))

        fun error(message: String): ChannelConfiguration {
            val config = JSONObject("""{"type": "error"}""")
            config.put("message", message)
            return ChannelConfiguration(config)
        }

        override fun ChannelConfiguration.write(parcel: Parcel, flags: Int) {
            parcel.writeString(toJson(includingSecrets = true).toString())
        }
        override fun create(parcel: Parcel): ChannelConfiguration {
            return ChannelConfiguration(JSONObject(parcel.readString()))
        }
    }
}
