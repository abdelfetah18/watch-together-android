package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.Asset
import com.abdelfetahdev.watch_together.entities.Room
import com.squareup.picasso.Picasso


class RoomActivity : AppCompatActivity() {
    lateinit var roomImage: ImageView
    lateinit var roomName: TextView
    lateinit var totalMembers: TextView
    lateinit var joinRoomForm: LinearLayout
    lateinit var roomMenu: GridLayout

    lateinit var goBackButton: ImageButton
    lateinit var chatButton: LinearLayout
    lateinit var watchButton: LinearLayout
    lateinit var membersButton: LinearLayout
    lateinit var roomSettingButton: LinearLayout
    lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        roomImage = findViewById(R.id.room_image)
        roomName = findViewById(R.id.room_name)
        totalMembers = findViewById(R.id.room_members_count)
        joinRoomForm = findViewById(R.id.join_room_form)
        roomMenu = findViewById(R.id.room_menu)

        goBackButton = findViewById(R.id.go_back_button)

        chatButton = findViewById(R.id.chat_button)
        watchButton = findViewById(R.id.watch_button)
        membersButton = findViewById(R.id.members_button)
        roomSettingButton = findViewById(R.id.room_setting_button)

        if (intent.extras != null) {
            val bundle = intent.getBundleExtra("room")
            if (bundle != null) {
                room = Room.fromBundle(bundle)
            }

            roomName.text = room.name
            totalMembers.text = (room.totalMembers ?: 0).toString()
            if (room.profileImage != null) {
                Picasso.get().load(room.profileImage?.url).placeholder(R.drawable.profile_4_3)
                    .into(roomImage)
            }

            val didJoin = intent.extras!!.getBoolean("didJoin", true)
            if (!didJoin) {
                roomMenu.visibility = View.GONE
                joinRoomForm.visibility = View.VISIBLE
            } else {
                roomMenu.visibility = View.VISIBLE
                joinRoomForm.visibility = View.GONE
            }
        }

        goBackButton.setOnClickListener {
            finish()
        }

        chatButton.setOnClickListener {
            val intent = Intent(this@RoomActivity, RoomChatActivity::class.java)
            intent.putExtra("room", room.toBundle())
            startActivity(intent)
        }

        watchButton.setOnClickListener {
            val intent = Intent(this@RoomActivity, SelectVideoActivity::class.java)
            intent.putExtra("room", room.toBundle())
            startActivity(intent)
        }

        membersButton.setOnClickListener {
            val intent = Intent(this@RoomActivity, MembersActivity::class.java)
            intent.putExtra("room", room.toBundle())
            startActivity(intent)
        }

        roomSettingButton.setOnClickListener {
            Toast.makeText(this, "Not Implemented Yet", Toast.LENGTH_LONG).show()
        }
    }
}