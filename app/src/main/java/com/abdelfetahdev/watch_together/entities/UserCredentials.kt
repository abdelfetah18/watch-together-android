package com.abdelfetahdev.watch_together.entities

import org.json.JSONObject

data class UserCredentials(private val username: String, private val password: String) {
    fun toJSON(): JSONObject {
        return JSONObject(
            mapOf(
                "username" to username,
                "password" to password
            )
        )
    }
}
