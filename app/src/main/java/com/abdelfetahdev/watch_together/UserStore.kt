package com.abdelfetahdev.watch_together

import android.content.Context

class UserStore(private val context: Context) {

    companion object {
        private const val PREF_NAME = "my_preferences_1"
        private const val KEY_TOKEN = "accessToken"
    }

    fun saveToken(token: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }
}
