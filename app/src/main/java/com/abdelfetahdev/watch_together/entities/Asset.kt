package com.abdelfetahdev.watch_together.entities

import org.json.JSONObject

data class Asset(val url: String) {
    fun toJSON(): JSONObject {
        return JSONObject(
            mapOf(
                "url" to url
            )
        )
    }

    companion object {
        fun fromJSON(json: JSONObject): Asset {
            return Asset(json.getString("url"))
        }
    }
}
