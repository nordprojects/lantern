package co.nordprojects.lantern.shared

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

/**
 * Created by Michael Colville on 31/01/2018.
 */

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
