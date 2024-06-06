package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.coroutineScope
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class CreateRoomActivity : AppCompatActivity() {
    var imagePath: String? = null

    lateinit var roomImage: ImageView
    lateinit var nameInput: EditText
    lateinit var descriptionInput: EditText
    lateinit var privacyInput: Spinner
    lateinit var selectImageButton: CardView
    lateinit var createRoomButton: LinearLayout
    lateinit var roomPasswordWrapper: LinearLayout

    lateinit var goBackButton: ImageView

    var selectedPrivacy: String = "public"
    // lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        roomImage = findViewById(R.id.room_image)
        nameInput = findViewById(R.id.room_name)
        descriptionInput = findViewById(R.id.room_description)
        privacyInput = findViewById(R.id.room_privacy)
        selectImageButton = findViewById(R.id.select_image_button)
        createRoomButton = findViewById(R.id.create_room_button)
        roomPasswordWrapper = findViewById(R.id.room_password_wrapper)
        goBackButton = findViewById(R.id.go_back_button)


        val privacyMenu = resources.getStringArray(R.array.select_privacy)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, privacyMenu
        )
        privacyInput.adapter = adapter

        privacyInput.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedPrivacy = privacyMenu[position]
                if (selectedPrivacy == "public") {
                    roomPasswordWrapper.visibility = View.GONE
                } else {
                    roomPasswordWrapper.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        selectImageButton.setOnClickListener {
            getContent.launch("image/*")
        }

        val myApp = application as MyApp
        val restRooms = RestRooms(myApp.httpClient)


        createRoomButton.setOnClickListener {
            lifecycle.coroutineScope.launch {
                val response = restRooms.createRoom(
                    Room(
                        "",
                        nameInput.text.toString(),
                        descriptionInput.text.toString(),
                        selectedPrivacy,
                        null, null, null, null
                    )
                )

                if (response.isError) {
                    Toast.makeText(this@CreateRoomActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                    if (response.invalidAuth) {
                        startActivity(Intent(this@CreateRoomActivity, SignInActivity::class.java))
                    }
                } else {
                    val room = response.data
                    if ((room != null) and (imagePath != null)) {
                        val uploadResponse =
                            restRooms.uploadRoomProfileImage(room?.id!!, imagePath!!)
                        if (uploadResponse.isError) {
                            Toast.makeText(
                                this@CreateRoomActivity,
                                response.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                this@CreateRoomActivity,
                                "Room Created Successfully",
                                Toast.LENGTH_LONG
                            ).show()

                            val intent = Intent(this@CreateRoomActivity, RoomActivity::class.java)
                            intent.putExtra("room", room.toBundle())
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(
                            this@CreateRoomActivity,
                            "Room Created Successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this@CreateRoomActivity, RoomActivity::class.java)
                        intent.putExtra("room", room?.toBundle())
                        startActivity(intent)
                    }
                }
            }
        }

        goBackButton.setOnClickListener {
            finish()
        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        if (uri != null) {
            val imageBitmap = getImageBitmapFromURI(uri)
            if (imageBitmap != null) {
                imagePath = saveImageToCache(imageBitmap)
                roomImage.setImageBitmap(imageBitmap)
            }
        }
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

    private fun createRoom() {

    }
}