package co.nordprojects.lantern.shared

import android.net.Uri
import org.json.JSONObject

/**
 * Created by Michael Colville on 31/01/2018.
 */

data class ChannelInfo(val id: String,
                       val name: String,
                       val description: String,
                       val imageUri: Uri? = null,
                       val customizable: Boolean = false) {
    constructor(json: JSONObject) : this(
            json.getString("id"),
            json.getString("name"),
            json.getString("description"),
            json.optString("image")?.let { Uri.parse(it) },
            json.getBoolean("customizable")
    )
    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "id" to id,
                "name" to name,
                "description" to description,
                "image" to imageUri?.toString(),
                "customizable" to customizable
        ))
    }
}
