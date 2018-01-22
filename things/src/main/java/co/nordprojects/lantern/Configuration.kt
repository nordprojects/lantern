package co.nordprojects.lantern

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.util.*

/**
 * Provides access to, and persists, projector configuration.
 *
 * Created by joerick on 16/01/18.
 */
class ConfigurationManager(val context: Context) {
    val CONFIG_FILE_PATH = "config.json"
    val appConfig = AppConfiguration()

    init {
        load()
    }

    private fun load(): Unit {
        val file = context.openFileInput(CONFIG_FILE_PATH)
        val jsonObject = JSONObject(file.reader().readText())

        appConfig.updateWithJson(jsonObject)
    }

    private fun save(): Unit {
        val file = context.openFileOutput(CONFIG_FILE_PATH, MODE_PRIVATE)
        file.writer().write(appConfig.toJson().toString(2))
    }
}

class AppConfiguration: Observable() {
    class Planes {
        var up = ChannelConfiguration.blank
        var forward = ChannelConfiguration.blank
        var down = ChannelConfiguration.blank

        fun toJson(): JSONObject {
            return JSONObject(mapOf<String, Any>(
                    "up" to up.toJson(),
                    "forward" to forward.toJson(),
                    "down" to down.toJson()
            ))
        }
    }
    val planes = Planes()

    fun updateWithJson(json: JSONObject) {
        val planesJson = json.getJSONObject("planes")

        planes.up = ChannelConfiguration(planesJson.getJSONObject("up"))
        planes.forward = ChannelConfiguration(planesJson.getJSONObject("forward"))
        planes.down = ChannelConfiguration(planesJson.getJSONObject("down"))

        setChanged()
        notifyObservers()
    }

    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "planes" to planes.toJson()
        ))
    }
}

@Parcelize
class ChannelConfiguration(val type: String, val settings: JSONObject, val secret: JSONObject?) : Parcelable {
    constructor(json: JSONObject) : this(
            json.getString("type"),
            jsonObjectRemovingSecret(json),
            json.optJSONObject("secret"))

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
            get() = ChannelConfiguration(JSONObject("{\"type\": \"blank\"}"))

        override fun ChannelConfiguration.write(parcel: Parcel, flags: Int) {
            parcel.writeString(toJson(includingSecrets = true).toString())
        }
        override fun create(parcel: Parcel): ChannelConfiguration {
            return ChannelConfiguration(JSONObject(parcel.readString()))
        }
    }
}

private fun jsonObjectRemovingSecret(json: JSONObject): JSONObject {
    val copy = json.clone()
    copy.remove("secret")
    return copy
}

fun JSONObject.clone(): JSONObject {
    return JSONObject(this, keys().asSequence().toList().toTypedArray())
}
