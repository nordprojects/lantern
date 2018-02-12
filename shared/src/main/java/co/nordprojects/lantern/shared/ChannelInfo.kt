package co.nordprojects.lantern.shared

import android.net.Uri
import org.json.JSONObject

/**
 * Created by Michael Colville on 31/01/2018.
 */

data class ChannelInfo(val id: String,
                       val name: String,
                       val description: String,
                       val imageUri: Uri? = null) {
    constructor(json: JSONObject) : this(
            json.getString("id"),
            json.getString("name"),
            json.getString("description"),
            json.optString("image")?.let { Uri.parse(it) }
    )
    fun toJson(): JSONObject {
        return JSONObject(mapOf(
                "id" to id,
                "name" to name,
                "description" to description,
                "image" to imageUri?.toString()
        ))
    }
}
