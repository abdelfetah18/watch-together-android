package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.adapters.ChatMessagesAdapter
import com.abdelfetahdev.watch_together.entities.ChatEventPayload
import com.abdelfetahdev.watch_together.entities.EventName
import com.abdelfetahdev.watch_together.entities.Message
import com.abdelfetahdev.watch_together.entities.Payload
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.entities.VideoPlayerAction
import com.abdelfetahdev.watch_together.entities.VideoPlayerData
import com.abdelfetahdev.watch_together.entities.VideoPlayerEventPayload
import com.abdelfetahdev.watch_together.entities.WSMessage
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import com.abdelfetahdev.watch_together.utilities.LoadingDialogue
import com.abdelfetahdev.watch_together.utilities.WebSocketClient
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.sql.Timestamp

class VideoPlayerActivity : AppCompatActivity() {
    lateinit var webSocketClient: WebSocketClient
    lateinit var videoView: VideoView
    lateinit var videoUrl: String
    lateinit var room: Room

    lateinit var playPauseButton: ImageButton
    lateinit var openChatButton: ImageView
    lateinit var closeChatButton: ImageButton
    lateinit var videoPlayerView: RelativeLayout
    lateinit var currentProgress: TextView
    lateinit var videoProgressBar: SeekBar
    lateinit var loadingDialogue: LoadingDialogue
    private lateinit var videoControlButton: ConstraintLayout

    lateinit var messageInput: EditText
    lateinit var sendButton: LinearLayout


    var isAdmin: Boolean = false
    private val progressHandler = Handler(Looper.getMainLooper())
    private val handler = Handler(Looper.getMainLooper())
    private var previousPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        loadingDialogue = LoadingDialogue(this)

        videoView = findViewById(R.id.video_view)
        playPauseButton = findViewById(R.id.play_pause_button)
        openChatButton = findViewById(R.id.open_chat_button)
        closeChatButton = findViewById(R.id.close_chat_button)
        videoPlayerView = findViewById(R.id.video_player)
        currentProgress = findViewById(R.id.current_progress)
        videoProgressBar = findViewById(R.id.video_progress)
        videoControlButton = findViewById(R.id.video_control)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)

        val bundle = intent.getBundleExtra("room")
        if (bundle != null) {
            room = Room.fromBundle(bundle)
        }

        val myApp = application as MyApp

        val chatMessagesAdapter =
            ChatMessagesAdapter(this, myApp.user?.id ?: "", ArrayList())
        val recyclerView: RecyclerView? = findViewById(R.id.chat_messages)
        recyclerView?.adapter = chatMessagesAdapter
        val restRooms = RestRooms(myApp.httpClient)

        lifecycle.coroutineScope.launch {
            val response = restRooms.getChatMessages(room.id ?: "")
            if (response.isError) {
                Toast.makeText(this@VideoPlayerActivity, response.message, Toast.LENGTH_LONG).show()
                if (response.invalidAuth) {
                    startActivity(Intent(this@VideoPlayerActivity, SignInActivity::class.java))
                }
            } else {
                val messages = response.data
                chatMessagesAdapter.insertMessages(messages ?: ArrayList<Message>())
                recyclerView?.scrollToPosition(recyclerView.adapter?.itemCount?.minus(1) ?: 0)
            }
        }

        val youtubeVideoUrl = intent.extras?.getString("videoUrl")

        if (youtubeVideoUrl != null) {
            loadingDialogue.show()
            val job = lifecycle.coroutineScope.launch {
                val response = restRooms.getYoutubeVideo(youtubeVideoUrl)
                loadingDialogue.hide()
                if (response.isError) {
                    Toast.makeText(
                        this@VideoPlayerActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                    if (response.invalidAuth) {
                        startActivity(
                            Intent(
                                this@VideoPlayerActivity,
                                SignInActivity::class.java
                            )
                        )
                    }
                } else {
                    val video = response.data
                    if (video != null) {
                        videoUrl = video.videoUrl
                        isAdmin = myApp.user?.id == room.admin?.id
                        Log.i("VideoPlayerActivity", "videoUrl=${videoUrl}, isAdmin=${isAdmin}")
                        Log.i("VideoPlayerActivity", "room=${room}, user=${myApp.user}")

                        if (isAdmin && videoUrl != "") {
                            videoView.setVideoURI(videoUrl.toUri())
                            videoView.start()
                            currentProgress.text =
                                "${convertToTimeFormat(0)}/${convertToTimeFormat(videoView.duration)}"
                        } else {
                            val wsMessage = WSMessage(
                                EventName.VIDEO_PLAYER,
                                Payload.VideoPlayer(
                                    VideoPlayerEventPayload(
                                        VideoPlayerAction.SYNC,
                                        VideoPlayerData("", 0.0)
                                    )
                                )
                            )
                            webSocketClient.send(wsMessage.toJSON())
                        }
                    }
                }
            }
        }

        videoView.setOnPreparedListener {
            videoProgressBar.max = it.duration
            progressHandler.post(updateProgressTask)
        }

        webSocketClient = WebSocketClient(room.id!!, myApp.accessToken)
        webSocketClient.connect(object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val jsonObject = JSONObject(text)
                Log.i("WebSocketListener", "onMessage: jsonObject=${jsonObject}")
                val wsMessage = WSMessage.fromJSON(jsonObject)
                Log.i("WebSocketListener", "onMessage: wsMessage=${wsMessage}")
                if (wsMessage.eventName == EventName.VIDEO_PLAYER) {
                    val payload = wsMessage.payload as Payload.VideoPlayer
                    if (!isAdmin) {
                        if (videoUrl != payload.event.data.videoUrl) {
                            videoUrl = payload.event.data.videoUrl
                            runOnUiThread {
                                videoView.setVideoURI(videoUrl.toUri())
                                videoView.seekTo(payload.event.data.timestamp.toInt())
                            }

                            handleAction(payload);
                        } else {
                            handleAction(payload);
                        }
                    } else {
                        if (payload.event.action == VideoPlayerAction.SYNC) {
                            var action: VideoPlayerAction = VideoPlayerAction.PAUSE
                            if (videoView.isPlaying) {
                                action = VideoPlayerAction.PLAY
                            }

                            val wsMessage = WSMessage(
                                EventName.VIDEO_PLAYER,
                                Payload.VideoPlayer(
                                    VideoPlayerEventPayload(
                                        action,
                                        VideoPlayerData(
                                            videoUrl,
                                            timestampToSeconds(videoView.currentPosition)
                                        )
                                    )
                                )
                            )
                            webSocketClient.send(wsMessage.toJSON())
                        }
                    }
                }

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

        playPauseButton.setOnClickListener {
            if (videoView.isPlaying) {
                pause()
            } else {
                play()
            }
        }

        openChatButton.setOnClickListener {
            animateWeightTransition(videoPlayerView, 1F)
        }

        closeChatButton.setOnClickListener {
            animateWeightTransition(videoPlayerView, 0F)
        }

        videoControlButton.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                handler.postDelayed(hideLinearLayoutRunnable, 0)
                return false
            }
        })

        videoView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                // Show the LinearLayout with fade-in animation
                val fadeIn = AnimationUtils.loadAnimation(
                    this@VideoPlayerActivity,
                    androidx.appcompat.R.anim.abc_fade_in
                )
                videoControlButton.startAnimation(fadeIn)
                videoControlButton.visibility = View.VISIBLE

                handler.postDelayed(hideLinearLayoutRunnable, 5000)
                return false
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

    private val updateProgressTask = object : Runnable {
        override fun run() {
            val currentPosition = videoView.currentPosition
            if (currentPosition != previousPosition) {
                val progressMessage =
                    "${convertToTimeFormat(currentPosition)}/${convertToTimeFormat(videoView.duration)}"
                findViewById<TextView>(R.id.current_progress).text = progressMessage
                videoProgressBar.progress = currentPosition
                previousPosition = currentPosition
            }
            progressHandler.postDelayed(this, 0)
        }
    }

    private val hideLinearLayoutRunnable = Runnable {
        val fadeOut = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_out)
        videoControlButton.startAnimation(fadeOut)
        videoControlButton.visibility = View.GONE
    }

    fun timestampToSeconds(timestamp: Int): Double {
        return timestamp.toDouble() / 1000
    }

    fun secondsToTimestamp(seconds: Double): Int {
        return (seconds * 1000).toInt()
    }

    fun handleAction(payload: Payload.VideoPlayer) {
        runOnUiThread {
            if (payload.event.action == VideoPlayerAction.PLAY) {
                play()
                videoView.seekTo(secondsToTimestamp(payload.event.data.timestamp))
            }

            if (payload.event.action == VideoPlayerAction.PAUSE) {
                pause();
                videoView.seekTo(secondsToTimestamp(payload.event.data.timestamp))
            }

            if (payload.event.action == VideoPlayerAction.START) {
                play();
                videoView.seekTo(secondsToTimestamp(payload.event.data.timestamp))
            }

            if (payload.event.action == VideoPlayerAction.UPDATE) {
                videoView.seekTo(secondsToTimestamp(payload.event.data.timestamp))
            }
        }
    }

    private fun convertToTimeFormat(current: Int): String {
        val totalSeconds = current / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    fun play() {
        videoView.start()
        playPauseButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause))
        if (isAdmin) {
            val wsMessage = WSMessage(
                EventName.VIDEO_PLAYER,
                Payload.VideoPlayer(
                    VideoPlayerEventPayload(
                        VideoPlayerAction.PLAY,
                        VideoPlayerData(
                            videoUrl,
                            timestampToSeconds(videoView.currentPosition)
                        )
                    )
                )
            )
            webSocketClient.send(wsMessage.toJSON())
        }
    }

    fun pause() {
        videoView.pause()
        playPauseButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_play))
        if (isAdmin) {
            val wsMessage = WSMessage(
                EventName.VIDEO_PLAYER,
                Payload.VideoPlayer(
                    VideoPlayerEventPayload(
                        VideoPlayerAction.PAUSE,
                        VideoPlayerData(
                            videoUrl,
                            timestampToSeconds(videoView.currentPosition)
                        )
                    )
                )
            )
            webSocketClient.send(wsMessage.toJSON())
        }
    }

    private fun animateWeightTransition(view: View, targetWeight: Float, duration: Long = 300L) {
        val anim = createWeightAnimation(view, targetWeight)
        anim.duration = duration
        view.startAnimation(anim)
    }

    private fun createWeightAnimation(view: View, targetWeight: Float): Animation {
        val anim = object : Animation() {
            private val initialWeight =
                (view.layoutParams as? LinearLayout.LayoutParams)?.weight ?: 0f

            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = view.layoutParams as? LinearLayout.LayoutParams
                params?.weight = initialWeight + (targetWeight - initialWeight) * interpolatedTime
                view.requestLayout()
            }
        }
        view.layoutParams = view.layoutParams // Ensure that layout params are set correctly
        return anim
    }

    override fun onPause() {
        super.onPause()
        // Make sure to remove any pending hide requests when the activity is paused
        handler.removeCallbacks(hideLinearLayoutRunnable)
    }
}