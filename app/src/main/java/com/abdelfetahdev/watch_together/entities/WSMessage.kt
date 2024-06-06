package com.abdelfetahdev.watch_together.entities

import android.util.Log
import org.json.JSONObject

enum class EventName { CHAT, VIDEO_PLAYER }
enum class VideoPlayerAction { PLAY, PAUSE, UPDATE, START, SYNC }

data class VideoPlayerData(val videoUrl: String, val timestamp: Double) {

    fun toJSON(): JSONObject {
        return JSONObject(
            mapOf(
                "video_url" to videoUrl,
                "timestamp" to timestamp
            )
        )
    }

    companion object {
        fun fromJSON(json: JSONObject): VideoPlayerData {
            return VideoPlayerData(json.getString("video_url"), json.getDouble("timestamp"))
        }
    }
}

data class VideoPlayerEventPayload(val action: VideoPlayerAction, val data: VideoPlayerData) {

    fun toJSON(): JSONObject {
        return JSONObject(
            mapOf(
                "action" to action.name.lowercase(),
                "data" to data.toJSON()
            )
        )
    }

    companion object {
        fun fromJSON(json: JSONObject): VideoPlayerEventPayload {
            val videoPlayerAction = when (json.getString("action")) {
                "play" -> VideoPlayerAction.PLAY
                "pause" -> VideoPlayerAction.PAUSE
                "update" -> VideoPlayerAction.UPDATE
                "start" -> VideoPlayerAction.START
                "sync" -> VideoPlayerAction.SYNC
                else -> VideoPlayerAction.SYNC
            }

            return VideoPlayerEventPayload(
                videoPlayerAction,
                VideoPlayerData.fromJSON(json.getJSONObject("data"))
            )
        }
    }
}

typealias ChatEventPayload = Message

sealed class Payload {
    data class Chat(val message: ChatEventPayload) : Payload()
    data class VideoPlayer(val event: VideoPlayerEventPayload) : Payload()
}

data class WSMessage(val eventName: EventName, val payload: Payload) {

    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()
        val eventNameString = if (eventName == EventName.CHAT) "chat" else "video_player"

        jsonObject.put("eventName", eventNameString)
        jsonObject.put(
            "payload",
            if (eventName == EventName.CHAT) (payload as Payload.Chat).message.toJSON() else (payload as Payload.VideoPlayer).event.toJSON()
        )

        return jsonObject
    }

    companion object {
        fun fromJSON(json: JSONObject): WSMessage {
            val eventName = json.getString("eventName")
            return WSMessage(
                if (eventName == "chat") EventName.CHAT else EventName.VIDEO_PLAYER,
                if (eventName == "chat") Payload.Chat(ChatEventPayload.fromJSON(json.getJSONObject("payload"))) else Payload.VideoPlayer(
                    VideoPlayerEventPayload.fromJSON(json.getJSONObject("payload"))
                )
            )
        }
    }
}
