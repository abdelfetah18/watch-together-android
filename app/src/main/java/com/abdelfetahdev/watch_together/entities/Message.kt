package com.abdelfetahdev.watch_together.entities

import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

data class Message(
    val message: String,
    val type: String,
    val user: User?,
    val createdAt: Date?
) {

    fun toJSON(): JSONObject {
        return JSONObject(
            mapOf(
                "message" to message,
                "type" to type
            )
        )
    }

    companion object {

        fun fromJSON(json: JSONObject): Message {
            return Message(
                json.getString("message"),
                json.getString("message"),
                User.fromJSON(json.getJSONObject("user")),
                null
            )
        }

        fun getListOfMessages(json: JSONArray): ArrayList<Message> {
            val messages = ArrayList<Message>()

            for (index in 0 until json.length()) {
                messages.add(
                    Message(
                        json.getJSONObject(index).getString("message"),
                        json.getJSONObject(index).getString("type"),
                        User.fromJSON(json.getJSONObject(index).getJSONObject("user")),
                        null
                    )
                )
            }

            return messages
        }
    }
}
