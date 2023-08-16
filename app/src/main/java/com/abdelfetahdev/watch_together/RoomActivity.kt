package com.abdelfetahdev.watch_together

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking


class RoomActivity : AppCompatActivity() {
    private lateinit var accessToken: String
    private var videos = mutableListOf<Video>()
    private lateinit var listView : RecyclerView
    private lateinit var adapter : YoutubeSearchAdapter

    private lateinit var room : Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()
        accessToken = store.getToken().toString()

        val extras = intent?.extras
        if (extras != null) {
            val roomId = extras.getString("room_id").toString()
            getRoomById(roomId)
        }

        val goBackBtn = findViewById<ImageButton>(R.id.go_back_btn)
        goBackBtn.setOnClickListener {
            val intent = Intent(baseContext, ProfileActivity::class.java)
            val bundle = Bundle()
            bundle.putString("access_token", accessToken)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        }

        val openVideoPlayerBtn : Button = findViewById(R.id.open_video_player_btn)
        openVideoPlayerBtn.setOnClickListener {
            if(myApplication.getUserInfo()._id != room.admin._id){
                val intent = Intent(this, YoutubePlayerActivity::class.java)
                val bundle = Bundle()
                bundle.putString("roomId", room._id)
                bundle.putBoolean("isAdmin", false)
                intent.putExtras(bundle)
                startActivity(intent)
            }else{
                Toast.makeText(this, "You are the admin, you need to chose video to play instead.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRoomById(room_id: String){
        runBlocking {
            val r = (application as MyApp).client.getRoomById(room_id)
            if(r != null){
                room = r
                runOnUiThread {
                    // Load room information to the screen
                    findViewById<TextView>(R.id.room_name).text = room.name
                    Picasso.get().load(room.profile_image).placeholder(R.drawable.profile_1_1).into(findViewById<ImageView>(R.id.room_image))

                    listView = findViewById(R.id.my_rooms)
                    listView.layoutManager = LinearLayoutManager(this@RoomActivity)
                    videos.add(Video("Asking Android Developers About Security", "https://www.youtube.com/watch?v=-X03UKo_obE", "https://i.ytimg.com/vi/-X03UKo_obE/hq720.jpg?sqp=-oaymwEcCNAFEJQDSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLBReSQy8ahA86FggeCa2LCIrIld6g"))

                    adapter = YoutubeSearchAdapter(room._id, (application as MyApp).getUserInfo()._id == room.admin._id, videos)
                    listView.adapter = adapter
                }
            }
        }
    }
}