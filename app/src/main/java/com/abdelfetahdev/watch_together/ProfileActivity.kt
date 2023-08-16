package com.abdelfetahdev.watch_together

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking


class ProfileActivity : AppCompatActivity() {
    private lateinit var accessToken: String
    private var rooms = mutableListOf<Room>()
    private lateinit var listView : RecyclerView
    private lateinit var adapter : MyRoomsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()
        accessToken = store.getToken().toString()

        val usernameView = findViewById<TextView>(R.id.username)
        val profileImageView = findViewById<ImageView>(R.id.profile_image)

        val user = myApplication.user

        usernameView.text = user.username
        if(user.profile_image != "null"){
            Picasso.get().load(user.profile_image).placeholder(R.drawable.profile_1_1).into(profileImageView)
        }

        val navBar = NavigationController(findViewById(R.id.home_btn), findViewById(R.id.profile_btn), findViewById(R.id.settings_btn), this)
        navBar.initNavigation("profile")

        listView = findViewById(R.id.my_rooms)
        listView.layoutManager = LinearLayoutManager(this)
        adapter = MyRoomsAdapter(rooms)
        listView.adapter = adapter

        getMyRooms()
    }

    private fun getMyRooms(){
        runBlocking {
            rooms.addAll((application as MyApp).client.getMyRooms())
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}