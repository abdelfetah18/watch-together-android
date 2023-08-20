package com.abdelfetahdev.watch_together

import android.app.Application
import android.widget.Toast
import kotlinx.coroutines.runBlocking
import okhttp3.WebSocketListener

class MyApp : Application() {
    companion object {
        lateinit var webSocketClient: WebSocketClient
        lateinit var webSocketListener: MyWebSocketListener
    }

    lateinit var userStore : UserStore
    lateinit var accessToken : String
    lateinit var user : User
    lateinit var client : Client

    override fun onCreate() {
        super.onCreate()

        userStore = UserStore(applicationContext)
        accessToken = userStore.getToken().toString()
        client = Client(accessToken)
    }

    fun getMyUserStore(): UserStore {
        if (userStore == null) {
            userStore = UserStore(this)
        }
        return userStore
    }

    fun connectToWebSocket(room_id : String, webSocketListener : WebSocketListener) : WebSocketClient {
        val socketUrl = "wss://watch-together-uvdn.onrender.com/?room_id=$room_id&access_token=$accessToken"
        println("socketUrl: $socketUrl")
        webSocketClient = WebSocketClient(socketUrl)
        webSocketClient.connect(webSocketListener)
        return webSocketClient
    }

    fun initUser(){
        runBlocking {
            val r : User? = client.getUser()
            if(r != null){
                user = r
                accessToken = client.accessToken
            }else{
                // TODO: Handle error by sending the user a login page.
            }
        }
    }

    fun getUserInfo() : User {
        return user
    }
}

