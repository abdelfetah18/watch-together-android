package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.adapters.MyRoomsAdapter
import com.abdelfetahdev.watch_together.adapters.RoomMembersAdapter
import com.abdelfetahdev.watch_together.entities.Member
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import com.abdelfetahdev.watch_together.rest_api.RestUsers
import kotlinx.coroutines.launch

class MembersActivity : AppCompatActivity() {
    lateinit var room: Room
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        val bundle = intent.getBundleExtra("room")
        if (bundle != null) {
            room = Room.fromBundle(bundle)
        }

        val myApp = application as MyApp
        val roomMembersAdapter = RoomMembersAdapter(this, ArrayList())
        val recyclerView: RecyclerView = findViewById(R.id.room_members_list)
        recyclerView.adapter = roomMembersAdapter

        val restRooms = RestRooms(myApp.httpClient)

        lifecycle.coroutineScope.launch {
            val response = restRooms.getRoomMembers(room.id ?: "")
            if (response.isError) {
                Toast.makeText(this@MembersActivity, response.message, Toast.LENGTH_LONG).show()
                if (response.invalidAuth) {
                    startActivity(Intent(this@MembersActivity, SignInActivity::class.java))
                }
            } else {
                val members = response.data
                roomMembersAdapter.insertMembers(members ?: ArrayList())
            }
        }
    }
}