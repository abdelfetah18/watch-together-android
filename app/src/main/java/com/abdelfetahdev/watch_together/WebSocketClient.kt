package com.abdelfetahdev.watch_together

import okhttp3.*
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient(url: String) {
    private val client: OkHttpClient = OkHttpClient()
    private val request: Request = Request.Builder().url(url).build()
    private lateinit var webSocket: WebSocket

    fun connect(webSocketListener: WebSocketListener){
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun send(data: String){
        println("send the following data:\n$data")
        webSocket.send(data)
    }
}
