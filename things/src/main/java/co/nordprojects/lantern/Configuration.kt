package co.nordprojects.lantern

import android.content.Context
import android.content.Context.MODE_PRIVATE
import org.json.JSONObject
import java.io.InputStreamReader
import android.databinding.BaseObservable
import android.databinding.Bindable

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

class AppConfiguration: BaseObservable() {
    class Planes {
        var up = ChannelConfigurationBlank
        var forward = ChannelConfigurationBlank
        var down = ChannelConfigurationBlank

        fun toJson(): JSONObject {
            return JSONObject(mapOf<String, Any>(
                    "up" to up.toJson(),
                    "forward" to forward.toJson(),
                    "down" to down.toJson()
            ))
        }
    }
    @Bindable val planes = Planes()

    fun updateWithJson(json: JSONObject) {
        val planesJson = json.getJSONObject("planes")

        planes.up = ChannelConfiguration(planesJson.getJSONObject("up"))
        planes.forward = ChannelConfiguration(planesJson.getJSONObject("forward"))
        planes.down = ChannelConfiguration(planesJson.getJSONObject("down"))

        notifyPropertyChanged(BR.planes)
    }

    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "planes" to planes.toJson()
        ))
    }
}

class ChannelConfiguration(val type: String, val settings: JSONObject, val secret: JSONObject?) {
    constructor(json: JSONObject) : this(
            json.getString("type"),
            jsonObjectRemovingSecret(json),
            json.optJSONObject("secret"))

    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "settings" to settings
        ))
    }
}

val ChannelConfigurationBlank = ChannelConfiguration(JSONObject("{\"type\": \"blank\"}"))

private fun jsonObjectRemovingSecret(json: JSONObject): JSONObject {
    val copy = JSONObject(json, json.keys().asSequence().toList().toTypedArray())
    copy.remove("secret")
    return copy
}
