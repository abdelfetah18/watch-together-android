package com.abdelfetahdev.watch_together.utilities

import android.util.Log
import okhttp3.*
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class WebSocketClient(private val roomId: String, private val accessToken: String) {
    private lateinit var webSocket: WebSocket

    companion object {
        const val WEB_SCOKET_URL = "wss://watch-together-uvdn.onrender.com/"
    }

    fun connect(webSocketListener: WebSocketListener) {
        val client = OkHttpClient()
        val request: Request =
            Request.Builder().url("${WEB_SCOKET_URL}?room_id=$roomId&access_token=${accessToken}")
                .build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun send(data: JSONObject) {
        Log.i("WebSocketClient", "data.toString()=$data")
        webSocket.send(data.toString())
    }
}
