package com.abdelfetahdev.watch_together

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.internal.notifyAll
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class YoutubePlayerActivity : AppCompatActivity() {
    private var videoUrl : String? = null
    private lateinit var accessToken: String
    private lateinit var videoPlayerURL : String
    private lateinit var videoView : VideoView
    private lateinit var videoControlBtn : ConstraintLayout
    private lateinit var videoProgressBar: SeekBar

    private lateinit var roomId : String
    private var isAdmin : Boolean = false

    private var chatMessages = mutableListOf<Message>()
    private lateinit var chatListView : RecyclerView
    private lateinit var chatAdapter : ChatAdapter

    private lateinit var webSocketClient: WebSocketClient

    private val progressHandler = Handler(Looper.getMainLooper())
    private val handler = Handler(Looper.getMainLooper())
    private var previousPosition = 0

    private lateinit var playPauseBtn : ImageButton

    private val hideLinearLayoutRunnable = Runnable {
        val fadeOut = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_out)
        videoControlBtn.startAnimation(fadeOut)
        videoControlBtn.visibility = View.GONE
    }

    private val updateProgressTask = object : Runnable {
        override fun run() {
            val currentPosition = videoView.currentPosition
            if (currentPosition != previousPosition) {
                // Video progress has changed
                val progressMessage = convertToTimeFormat(currentPosition)
                findViewById<TextView>(R.id.current_progress).text = progressMessage
                // videoProgressBar.progress = currentPosition
                previousPosition = currentPosition
            }
            progressHandler.postDelayed(this, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_player)

        videoView = findViewById(R.id.video_view)
        videoControlBtn = findViewById(R.id.video_control)
        videoProgressBar = findViewById(R.id.video_progress)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()
        accessToken = store.getToken().toString()

        val extras = intent?.extras
        if (extras != null) {
            videoUrl = extras.getString("videoUrl")
            println("videoUrl: $videoUrl")
            roomId = extras.getString("roomId").toString()
            isAdmin = extras.getBoolean("isAdmin")
            Toast.makeText(this, "isAdmin: $isAdmin", Toast.LENGTH_SHORT).show()

            chatListView = findViewById(R.id.chat_list_view)
            chatListView.layoutManager = LinearLayoutManager(this)
            chatAdapter = ChatAdapter(myApplication.getUserInfo()._id, chatMessages)
            chatListView.adapter = chatAdapter

            webSocketClient = myApplication.connectToWebSocket(roomId, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    // WebSocket connection is established
                    // You can perform any necessary initialization here
                    println("WebSocket Connection is Open.")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    println("text: $text")
                    // Handle incoming text message
                    val data = JSONObject(text)
                    when(data.getString("eventName")){
                        "chat" -> {
                            val message = JSONObject(data.getString("payload"))
                            val user = message.getJSONObject("user")
                            chatMessages.add(
                                Message(
                                    User(
                                        user.getString("_id"),
                                        user.getString("username"),
                                        user.optString("profile_image","null")
                                    ),
                                    message.getString("message"),
                                    message.getString("type"),
                                    message.getString("_createdAt")
                                )
                            )

                            runOnUiThread {
                                chatAdapter.notifyAll()
                            }
                        }

                        "video_player" -> {
                            val payload = JSONObject(data.getString("payload"))
                            val action = payload.getString("action")
                            runOnUiThread {
                                when(action){
                                    "play" -> {
                                        val videoPlayerData = payload.getJSONObject("video_player_data")
                                        val videoId = videoPlayerData.getString("video_id")
                                        setVideoUrlFromId(videoId)
                                        getVideoInfo()
                                    }
                                    "pause" -> {
                                        val videoPlayerData = payload.getJSONObject("video_player_data")
                                        val timestamp = videoPlayerData.getDouble("timestamp")
                                        val videoId = videoPlayerData.getString("video_id")
                                        if(videoUrl == getVideoUrlFromId(videoId)) {
                                            if (videoView.isPlaying) {
                                                videoView.seekTo((timestamp*1000).toInt())
                                                pauseVideo()
                                            }
                                        }else{
                                            setVideoUrlFromId(videoId)
                                            handleVideoPlayer(action, timestamp)
                                            println("videoUrl: $videoUrl")
                                            println("timestamp: $timestamp")
                                        }
                                    }
                                    "update" -> {
                                        val videoPlayerData = payload.getJSONObject("video_player_data")
                                        val timestamp = videoPlayerData.getDouble("timestamp")
                                        val videoId = videoPlayerData.getString("video_id")
                                        if(videoUrl == getVideoUrlFromId(videoId)){
                                            videoView.seekTo((timestamp*1000).toInt())
                                        }else{
                                            setVideoUrlFromId(videoId)
                                            handleVideoPlayer(action, timestamp)
                                            println("videoUrl: $videoUrl")
                                            println("timestamp: $timestamp")
                                        }
                                    }
                                    "start" -> {
                                        val videoPlayerData = payload.getJSONObject("video_player_data")
                                        val videoId = videoPlayerData.getString("video_id")
                                        val timestamp = videoPlayerData.getDouble("timestamp")
                                        if(videoUrl == getVideoUrlFromId(videoId)){
                                            videoView.seekTo((timestamp*1000).toInt())
                                            playVideo()
                                        }else{
                                            setVideoUrlFromId(videoId)
                                            handleVideoPlayer(action, timestamp)
                                            println("videoUrl: $videoUrl")
                                            println("timestamp: $timestamp")
                                        }
                                     }
                                    "sync" -> {
                                        val actionState: String = if(videoView.isPlaying) "start" else "pause"
                                        val videoId = getVideoId()

                                        val timestamp = videoView.currentPosition / 1000
                                        val payloadR =
                                            "{ \"action\": $actionState, \"video_player_data\": { \"timestamp\": ${timestamp}, \"video_id\": \"$videoId\" }}"
                                        webSocketClient.send("{\"eventName\":\"video_player\",\"payload\": $payloadR }")
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    // WebSocket connection is about to close
                    // You can perform any necessary cleanup here
                    println("WebSocket Connection is Closed.")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    // WebSocket connection failure
                    // You can handle the failure here
                    println("WebSocket Connection is Failed. Error: $t")
                }
            })
        }

        val closeChatBtn : ImageButton = findViewById(R.id.close_chat_btn)
        val openChatBtn = findViewById<ImageButton>(R.id.open_chat_btn)
        val videoPlayer = findViewById<RelativeLayout>(R.id.video_player)
        val messageInput = findViewById<EditText>(R.id.message_input)

        closeChatBtn.setOnClickListener {
            animateWeightTransition(videoPlayer, 0F)
        }

        openChatBtn.setOnClickListener {
            animateWeightTransition(videoPlayer, 1F)
        }

        messageInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                webSocketClient.send("{\"eventName\":\"chat\", \"payload\":{ \"message\":\"${messageInput.text}\", \"type\": \"text\"}}")

                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDateTime = dateFormat.format(calendar.time)

                chatMessages.add(Message(myApplication.getUserInfo(), messageInput.text.toString(), "text", formattedDateTime))
                chatAdapter.notifyAll()

                messageInput.text.clear()

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        playPauseBtn = findViewById(R.id.play_pause_btn)

        playPauseBtn.setOnClickListener {
            if(videoView.isPlaying){
                pauseVideo()
                if(isAdmin){
                    val videoId = getVideoId()
                    val timestamp = videoView.currentPosition / 1000
                    val payload =
                        "{ \"action\": \"pause\", \"video_player_data\": { \"timestamp\": ${timestamp}, \"video_id\": \"$videoId\" }}"
                    println("payload: $payload")
                    webSocketClient.send("{\"eventName\":\"video_player\",\"payload\": $payload }")
                }
            }else{
                playVideo()
                if(isAdmin){
                    val videoId = getVideoId()
                    val timestamp = videoView.currentPosition / 1000
                    val payload =
                        "{ \"action\": \"start\", \"video_player_data\": { \"timestamp\": ${timestamp}, \"video_id\": \"$videoId\" }}"
                    println("payload: $payload")
                    webSocketClient.send("{\"eventName\":\"video_player\",\"payload\": $payload }")
                }
            }
        }

        videoControlBtn.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                handler.postDelayed(hideLinearLayoutRunnable, 0)
                return false
            }
        })

        videoView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                // Show the LinearLayout with fade-in animation
                val fadeIn = AnimationUtils.loadAnimation(this@YoutubePlayerActivity, androidx.appcompat.R.anim.abc_fade_in)
                videoControlBtn.startAnimation(fadeIn)
                videoControlBtn.visibility = View.VISIBLE

                handler.postDelayed(hideLinearLayoutRunnable, 5000)
                return false
            }
        })

        videoView.setOnPreparedListener {
            // Start tracking video progress when the video is prepared
            videoProgressBar.max = it.duration
            progressHandler.post(updateProgressTask)
        }

        videoProgressBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    videoView.seekTo(progress)
                    if(isAdmin){
                        val videoId = getVideoId()
                        val timestamp = videoView.currentPosition / 1000
                        val payload =
                            "{ \"action\": \"update\", \"video_player_data\": { \"timestamp\": ${timestamp}, \"video_id\": \"$videoId\" }}"
                        println("payload: $payload")
                        webSocketClient.send("{\"eventName\":\"video_player\",\"payload\": $payload }")
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        getVideoInfo()
        getChatMessages()
    }

    override fun onPause() {
        super.onPause()
        // Make sure to remove any pending hide requests when the activity is paused
        handler.removeCallbacks(hideLinearLayoutRunnable)
    }

    fun getVideoInfo(){
        runBlocking {
            val videoPlayerUrl = (application as MyApp).client.getVideoInfo(videoUrl ?: "")
            if(videoPlayerUrl != null){
                videoPlayerURL = videoPlayerUrl
                runOnUiThread {
                    val uri: Uri = Uri.parse(videoPlayerURL)
                    videoView.setVideoURI(uri)
                    playVideo()
                }
            }
        }
    }

    fun handleVideoPlayer(action: String,timestamp: Double){
        runBlocking {
            val videoPlayerUrl = (application as MyApp).client.getVideoInfo(videoUrl ?: "")
            if(videoPlayerUrl != null){
                videoPlayerURL = videoPlayerUrl
                runOnUiThread {
                    val uri: Uri = Uri.parse(videoPlayerURL)
                    videoView.setVideoURI(uri)
                    when(action){
                        "start" -> {
                            videoView.seekTo((timestamp*1000).toInt())
                            playVideo()
                        }
                        "update" -> {
                            videoView.seekTo((timestamp*1000).toInt())
                        }
                        "pause" -> {
                            videoView.seekTo((timestamp*1000).toInt())
                            pauseVideo()
                        }
                    }
                }
            }
        }
    }

    private fun getChatMessages(){
        runBlocking {
            (application as MyApp).client.getChatMessages(roomId)
        }
    }

    private fun animateWeightTransition(view: View, targetWeight: Float, duration: Long = 300L) {
        val anim = createWeightAnimation(view, targetWeight)
        anim.duration = duration
        view.startAnimation(anim)
    }

    private fun createWeightAnimation(view: View, targetWeight: Float): Animation {
        val anim = object : Animation() {
            private val initialWeight = (view.layoutParams as? LinearLayout.LayoutParams)?.weight ?: 0f

            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = view.layoutParams as? LinearLayout.LayoutParams
                params?.weight = initialWeight + (targetWeight - initialWeight) * interpolatedTime
                view.requestLayout()
            }
        }
        view.layoutParams = view.layoutParams // Ensure that layout params are set correctly
        return anim
    }

    private fun setVideoUrlFromId(videoId : String){
        videoUrl = "https://www.youtube.com/watch?v=$videoId"
    }

    private fun getVideoUrlFromId(videoId : String) : String {
        return "https://www.youtube.com/watch?v=$videoId"
    }

    private fun getVideoId() : String {
        val url = videoUrl?.toUri()
        val queryParams = url?.query
        val queryParamsMap = queryParams?.split('&')?.associate {
            val (key, value) = it.split('=')
            key to value
        }

        return queryParamsMap?.get("v") ?: ""
    }

    private fun convertToTimeFormat(current : Int) : String {
        val totalSeconds = current / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playVideo(){
        videoView.start()
        playPauseBtn.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.pause))
        playPauseBtn.background = AppCompatResources.getDrawable(this,R.drawable.pause)
    }

    private fun pauseVideo(){
        videoView.pause()
        playPauseBtn.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play))
        playPauseBtn.background = AppCompatResources.getDrawable(this, R.drawable.play)
    }
}