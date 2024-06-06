package com.abdelfetahdev.watch_together

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.abdelfetahdev.watch_together.entities.User
import com.abdelfetahdev.watch_together.entities.UserSession
import com.abdelfetahdev.watch_together.rest_api.RestUsers
import com.abdelfetahdev.watch_together.utilities.HttpClient
import com.abdelfetahdev.watch_together.utilities.MyWebSocketListener
import com.abdelfetahdev.watch_together.utilities.WebSocketClient
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.WebSocketListener

class MyApp : Application() {
    companion object {
        const val SHARED_PREFS = "SHARED_PREFS"
    }

    lateinit var accessToken: String
    lateinit var httpClient: HttpClient
    var user: User? = null

    override fun onCreate() {
        super.onCreate()

        loadAccessToken()
        httpClient = HttpClient(OkHttpClient(), accessToken)
        if (accessToken.length > 0) {
            loadUser()
        }
    }

    fun loadAccessToken() {
        val prefs: SharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        accessToken = prefs.getString(UserSession.ACCESS_TOKEN, "") ?: ""
        Log.i("MyApp", "accessToken=$accessToken")
    }

    fun loadUser() {
        val restUsers = RestUsers(httpClient)
        runBlocking {
            val response = restUsers.getCurrentUser()
            if (!response.isError) {
                if (response.data != null) {
                    user = response.data
                }
            }
        }
    }
}

