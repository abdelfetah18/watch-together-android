package com.abdelfetahdev.watch_together

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream


class CreateRoomActivity : AppCompatActivity() {
    private var roomImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val profileImage: ImageView = findViewById(R.id.room_image)
        val editProfileImageBtn = findViewById<Button>(R.id.edit_profile_image_btn)
        val createRoomBtn = findViewById<Button>(R.id.create_room_btn)
        val goBackBtn = findViewById<ImageButton>(R.id.go_back_btn)

        goBackBtn.setOnClickListener { goBack() }
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


    private fun getImageBitmapFromURI(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): String {
        val cacheDir = cacheDir
        val imageName = "cached_image_${System.currentTimeMillis()}.jpg"
        val imagePath = File(cacheDir, imageName)
        try {
            val outputStream = FileOutputStream(imagePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imagePath.absolutePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == SELECT_PICTURE){
            val imageURI = data?.data
            if(imageURI != null){
                val imageBitmap = getImageBitmapFromURI(imageURI)
                if(imageBitmap != null){
                    val imagePath = saveImageToCache(imageBitmap)
                    findViewById<ImageView>(R.id.room_image).setImageBitmap(imageBitmap)
                    roomImage = imagePath
                }
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
            val response = (application as MyApp).client.createRoom(name,description,privacy,password)
            if(response != null){
                val status = response.optString("status")
                if(status == "success"){
                    Toast.makeText(this@CreateRoomActivity, "Room created.", Toast.LENGTH_SHORT).show()
                    if(roomImage != null){
                        val room = response.getJSONObject("data")
                        val roomId = room.getString("_id")
                        val res = (application as MyApp).client.uploadRoomProfileImage(roomId,
                            roomImage!!
                        )
                        val intent = Intent(this@CreateRoomActivity, RoomActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("room_id", roomId)
                        intent.putExtras(bundle)
                        this@CreateRoomActivity.startActivity(intent)
                        this@CreateRoomActivity.finish()
                    }
                    // TODO: Start Room Activity with the created room_id.

                }else{
                    val message = response.getString("message")
                    Toast.makeText(this@CreateRoomActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goBack(){
        val intent = Intent(baseContext, ProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }
}