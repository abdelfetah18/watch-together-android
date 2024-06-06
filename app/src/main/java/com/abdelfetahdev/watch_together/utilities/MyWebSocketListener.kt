package com.abdelfetahdev.watch_together.utilities

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class MyWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // WebSocket connection is established
        // You can perform any necessary initialization here
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Handle incoming text message
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        // Handle incoming binary message
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // WebSocket connection is about to close
        // You can perform any necessary cleanup here
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // WebSocket connection failure
        // You can handle the failure here
    }
}
