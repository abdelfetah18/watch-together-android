package com.abdelfetahdev.watch_together.entities

import org.json.JSONObject

data class User(
    val id: String?,
    val username: String,
    val email: String?,
    val password: String?,
    val profileImage: Asset?
) {
    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()

        jsonObject.put("username", username)
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        jsonObject.put("profileImage", profileImage?.toJSON() ?: JSONObject.NULL)

        return jsonObject
    }

    companion object {
        fun fromJSON(json: JSONObject): User {
            val profileImage = json.optJSONObject("profile_image")

            return User(
                json.getString("_id"),
                json.getString("username"),
                json.optString("email"),
                null,
                if (profileImage != null) Asset(profileImage.optString("url")) else null,
            )
        }
    }
}
