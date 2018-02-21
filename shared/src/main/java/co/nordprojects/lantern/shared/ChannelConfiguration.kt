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
                           val settings: JSONObject = JSONObject(),
                           val secrets: JSONObject? = JSONObject()) : Parcelable {
    constructor(json: JSONObject) : this(
            json.getString("type"),
            json.optJSONObject("settings") ?: JSONObject(),
            json.optJSONObject("secrets")
    )

    fun toJson(includingSecrets: Boolean = false): JSONObject {
        val json = JSONObject()
        json.put("type", type)
        json.put("settings", settings)
        if (includingSecrets) {
            json.put("secrets", secrets)
        }
        return json
    }

    companion object : Parceler<ChannelConfiguration> {
        val blank: ChannelConfiguration
            get() = ChannelConfiguration(type = "blank")

        fun error(message: String): ChannelConfiguration {
            return ChannelConfiguration(
                    type = "error",
                    settings = JSONObject(mapOf("message" to message))
            )
        }

        override fun ChannelConfiguration.write(parcel: Parcel, flags: Int) {
            parcel.writeString(toJson(includingSecrets = true).toString())
        }
        override fun create(parcel: Parcel): ChannelConfiguration {
            return ChannelConfiguration(JSONObject(parcel.readString()))
        }
    }
}
