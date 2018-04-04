package com.example.androidthings.lantern.shared

import android.net.Uri
import org.json.JSONObject

/**
 * Metadata about a channel.
 *
 * Provided by the things app for display within the mobile app in the channel list.
 */
data class ChannelInfo(val id: String,
                       val name: String,
                       val description: String,
                       val imageUri: Uri? = null,
                       val customizable: Boolean = false,
                       val rotationDisabled: Boolean = false) {
    constructor(json: JSONObject) : this(
            json.getString("id"),
            json.getString("name"),
            json.getString("description"),
            (json.opt("image") as? String)?.let { Uri.parse(it) },
            json.getBoolean("customizable"),
            json.getBoolean("rotationDisabled")
    )
    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "id" to id,
                "name" to name,
                "description" to description,
                "image" to imageUri?.toString(),
                "customizable" to customizable,
                "rotationDisabled" to rotationDisabled
        ))
    }
}
