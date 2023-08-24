package com.abdelfetahdev.watch_together

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking


class CreateRoomActivity : AppCompatActivity() {
    private val roomImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val profileImage: ImageView = findViewById(R.id.room_image)
        val editProfileImageBtn = findViewById<Button>(R.id.edit_profile_image_btn)
        val createRoomBtn = findViewById<Button>(R.id.create_room_btn)

        editProfileImageBtn.setOnClickListener { pickImage() }
        createRoomBtn.setOnClickListener { createRoom() }
    }

    companion object {
        const val SELECT_PICTURE = 200
    }
    private fun pickImage(){
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == SELECT_PICTURE){
            val imageURI = data?.data
            if(imageURI != null){
                findViewById<ImageView>(R.id.room_image).setImageURI(imageURI)
            }
        }
    }

    private fun createRoom(){
        val nameInput = findViewById<EditText>(R.id.room_name)
        val descriptionInput = findViewById<EditText>(R.id.room_description)
        val isPrivate = findViewById<Switch>(R.id.is_private_switch)
        val passwordInput = findViewById<EditText>(R.id.room_password)

        val name = nameInput.text.toString()
        val description = descriptionInput.text.toString()
        val privacy = if(isPrivate.isChecked) "private" else "public"
        val password = passwordInput.text.toString()

        runBlocking {
            (application as MyApp).client.createRoom(name,description,privacy,password)
        }
    }
}