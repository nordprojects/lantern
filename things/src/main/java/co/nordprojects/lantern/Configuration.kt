package co.nordprojects.lantern

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
import java.io.FileNotFoundException
import java.util.*

/**
 * Provides access to, and persists, projector configuration.
 *
 * Created by joerick on 16/01/18.
 */
class ConfigurationManager(val context: Context) {
    val TAG = ConfigurationManager::class.java.simpleName
    val CONFIG_FILE_PATH = "config.json"
    val appConfig = AppConfiguration()
    val appConfigObserver = Observer { _, _ -> save() }

    init {
        load()
        appConfig.addObserver(appConfigObserver)
    }

    private fun load(): Unit {
        try {
            val file = context.openFileInput(CONFIG_FILE_PATH)
            val jsonObject = JSONObject(file.reader().readText())
            file.close()
            appConfig.updateWithJson(jsonObject)
        }
        catch (e: FileNotFoundException) {
            Log.w(TAG, "Failed to load settings file.", e)
        }
    }

    private fun save(): Unit {
        val file = context.openFileOutput(CONFIG_FILE_PATH, MODE_PRIVATE)
        val fileWriter = file.writer()
        fileWriter.write(appConfig.toJson().toString(2))
        fileWriter.close()
        file.close()

        Log.d(TAG, "Saved settings to ${CONFIG_FILE_PATH}")
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

private fun jsonObjectRemovingSecret(json: JSONObject): JSONObject {
    val copy = json.clone()
    copy.remove("secret")
    return copy
}

fun JSONObject.clone(): JSONObject {
    return JSONObject(this, keys().asSequence().toList().toTypedArray())
}
