package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.adapters.ChatMessagesAdapter
import com.abdelfetahdev.watch_together.adapters.ExploreRoomsAdapter
import com.abdelfetahdev.watch_together.entities.Asset
import com.abdelfetahdev.watch_together.entities.ChatEventPayload
import com.abdelfetahdev.watch_together.entities.EventName
import com.abdelfetahdev.watch_together.entities.Message
import com.abdelfetahdev.watch_together.entities.Payload
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.entities.WSMessage
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import com.abdelfetahdev.watch_together.utilities.WebSocketClient
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

class RoomChatActivity : AppCompatActivity() {
    lateinit var goBackButton: ImageView
    lateinit var roomName: TextView
    lateinit var roomImage: ImageView
    lateinit var onlineMembers: TextView
    lateinit var messageInput: EditText
    lateinit var sendButton: LinearLayout
    lateinit var room: Room
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_chat)

        val bundle = intent.getBundleExtra("room")
        if (bundle != null) {
            room = Room.fromBundle(bundle)
        }

        goBackButton = findViewById(R.id.go_back_button)

        roomName = findViewById(R.id.room_name)
        roomImage = findViewById(R.id.room_image)
        onlineMembers = findViewById(R.id.online_members)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)

        goBackButton.setOnClickListener {
            finish()
        }

        roomName.text = room.name
        if (room.profileImage != null) {
            Picasso.get().load("${room.profileImage?.url}?h=400&w=400&fit=crop&crop=center")
                .placeholder(R.drawable.profile_1_1)
                .into(roomImage)
        }

        val myApp = application as MyApp

        val chatMessagesAdapter =
            ChatMessagesAdapter(this, myApp.user?.id ?: "", ArrayList<Message>())
        val recyclerView: RecyclerView? = findViewById(R.id.chat_messages)
        recyclerView?.adapter = chatMessagesAdapter

        val restRooms = RestRooms(myApp.httpClient)

        lifecycle.coroutineScope.launch {
            val response = restRooms.getChatMessages(room.id ?: "")
            if (response.isError) {
                Toast.makeText(this@RoomChatActivity, response.message, Toast.LENGTH_LONG).show()
                if (response.invalidAuth) {
                    startActivity(Intent(this@RoomChatActivity, SignInActivity::class.java))
                }
            } else {
                val messages = response.data
                chatMessagesAdapter.insertMessages(messages ?: ArrayList<Message>())
                recyclerView?.scrollToPosition(recyclerView.adapter?.itemCount?.minus(1) ?: 0)
            }
        }

        val webSocketClient = WebSocketClient(room.id!!, myApp.accessToken)
        webSocketClient.connect(object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val jsonObject = JSONObject(text)
                Log.i("WebSocketListener", "onMessage: jsonObject=${jsonObject}")
                val wsMessage = WSMessage.fromJSON(jsonObject)
                Log.i("WebSocketListener", "onMessage: wsMessage=${wsMessage}")
                if (wsMessage.eventName == EventName.CHAT) {
                    val payload = wsMessage.payload as Payload.Chat
                    runOnUiThread {
                        chatMessagesAdapter.insertMessage(payload.message)
                        recyclerView?.scrollToPosition(
                            recyclerView.adapter?.itemCount?.minus(1) ?: 0
                        )
                    }
                }
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.i("WebSocketListener", "onOpen")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e("WebSocketListener", "onFailure")
                Log.e("WebSocketListener", "onFailure: t=${t}, response=${response}")
            }
        })

        sendButton.setOnClickListener {
            val wsMessage = WSMessage(
                EventName.CHAT,
                Payload.Chat(ChatEventPayload(messageInput.text.toString(), "text", null, null))
            )

            chatMessagesAdapter.insertMessage(
                Message(
                    messageInput.text.toString(),
                    "text",
                    myApp.user,
                    null
                )
            )

            webSocketClient.send(wsMessage.toJSON())
            messageInput.text.clear()
            recyclerView?.scrollToPosition(recyclerView.adapter?.itemCount?.minus(1) ?: 0)
        }
    }
}