package com.abdelfetahdev.watch_together.entities

data class UserSession(val access_token: String) {
    companion object {
        const val ACCESS_TOKEN = "access_token"
    }
}
