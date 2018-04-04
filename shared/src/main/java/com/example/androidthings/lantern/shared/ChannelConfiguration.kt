package com.example.androidthings.lantern.shared

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

/**
 * Represents the configuration of a channel, including the channel type and any other settings
 * required.
 *
 * Normally created by the mobile app, and then sent to the things app for use.
 */
@SuppressLint("ParcelCreator")
@Parcelize
class ChannelConfiguration(val type: String,
                           val rotation: Rotation = Rotation.LANDSCAPE,
                           val settings: JSONObject = JSONObject(),
                           val secrets: JSONObject? = JSONObject()) : Parcelable {
    constructor(json: JSONObject) : this(
            json.getString("type"),
            Rotation.withJsonName(json.optString("rotation", "landscape")),
            json.optJSONObject("settings") ?: JSONObject(),
            json.optJSONObject("secrets")
    )

    fun toJson(includingSecrets: Boolean = false): JSONObject {
        val json = JSONObject()
        json.put("type", type)
        json.put("rotation", rotation.jsonName)
        json.put("settings", settings)
        if (includingSecrets) {
            json.put("secrets", secrets)
        }
        return json
    }

    fun copy(type: String = this.type,
             rotation: Rotation = this.rotation,
             settings: JSONObject = this.settings,
             secrets: JSONObject? = this.secrets): ChannelConfiguration {
        return ChannelConfiguration(type, rotation, settings, secrets)
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
