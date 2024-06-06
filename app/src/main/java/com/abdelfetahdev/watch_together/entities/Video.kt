package com.abdelfetahdev.watch_together.entities

import org.json.JSONObject

data class Video(val videoUrl: String) {
    companion object {
        fun fromJSON(json: JSONObject): Video {
            return Video(
                json.getString("url")
            )
        }
    }
}
