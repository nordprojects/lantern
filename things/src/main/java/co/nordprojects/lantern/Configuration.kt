package co.nordprojects.lantern

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import co.nordprojects.lantern.shared.clone
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
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
        catch (e: JSONException) {
            Log.w(TAG, "Corrupt settings file", e)
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
    private val _planes = mutableMapOf<Direction, ChannelConfiguration>(
            Direction.UP to ChannelConfiguration.blank,
            Direction.FORWARD to ChannelConfiguration.blank,
            Direction.DOWN to ChannelConfiguration.blank
    )
    val planes: Map<Direction, ChannelConfiguration>
        get() = _planes

    fun updatePlane(jsonDirection: String, jsonConfig: JSONObject, skipNotify: Boolean = false) {
        val direction = when (jsonDirection) {
            "up" -> Direction.UP
            "forward" -> Direction.FORWARD
            "down" -> Direction.DOWN
            else -> { throw IllegalArgumentException("Unknown direction $jsonDirection") }
        }

        _planes[direction] = ChannelConfiguration(jsonConfig)

        setChanged()
        if (!skipNotify) {
            notifyObservers()
        }
    }

    fun updateWithJson(json: JSONObject) {
        val planesJson = json.getJSONObject("planes")

        updatePlane("up", planesJson.getJSONObject("up"), skipNotify = true)
        updatePlane("forward", planesJson.getJSONObject("forward"), skipNotify = true)
        updatePlane("down", planesJson.getJSONObject("down"), skipNotify = true)

        notifyObservers()
    }

    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "planes" to mapOf(
                        "up" to planes[Direction.UP]?.toJson(includingSecrets = true),
                        "forward" to planes[Direction.FORWARD]?.toJson(includingSecrets = true),
                        "down" to planes[Direction.DOWN]?.toJson(includingSecrets = true)
                )
        ))
    }
}

@SuppressLint("ParcelCreator")
@Parcelize
class ChannelConfiguration(val type: String, val settings: JSONObject, val secret: JSONObject?) : Parcelable {
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
