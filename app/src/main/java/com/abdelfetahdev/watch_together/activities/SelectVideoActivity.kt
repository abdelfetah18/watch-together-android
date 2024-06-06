package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.Room

class SelectVideoActivity : AppCompatActivity() {
    lateinit var goBackButton: ImageView
    lateinit var youtubeVideoUrlInput: EditText
    lateinit var openYoutubeVideoUrlButton: Button
    lateinit var youtubeSearchInput: EditText
    lateinit var youtubeSearchButton: Button

    lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_video)

        val bundle = intent.getBundleExtra("room")
        if (bundle != null) {
            room = Room.fromBundle(bundle)
        }

        goBackButton = findViewById(R.id.go_back_button)
        youtubeVideoUrlInput = findViewById(R.id.youtube_video_url_input)
        openYoutubeVideoUrlButton = findViewById(R.id.open_youtube_video_url_button)
        youtubeSearchInput = findViewById(R.id.youtube_search_input)
        youtubeSearchButton = findViewById(R.id.youtube_search_button)


        goBackButton.setOnClickListener { finish() }
        youtubeSearchButton.setOnClickListener {
            val intent = Intent(this, SearchYoutubeVideosActivity::class.java)
            if (youtubeSearchInput.text.isNotEmpty()) {
                intent.putExtra("room", room.toBundle())
                intent.putExtra("query", youtubeSearchInput.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "You must fill the input first", Toast.LENGTH_LONG).show()
            }
        }

        openYoutubeVideoUrlButton.setOnClickListener {
            if (youtubeVideoUrlInput.text.isNotEmpty()) {
                if (isValidYouTubeUrl(youtubeVideoUrlInput.text.toString())) {
                    val intent = Intent(this, VideoPlayerActivity::class.java)

                    intent.putExtra("room", room.toBundle())
                    intent.putExtra("videoUrl", youtubeVideoUrlInput.text.toString())

                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid Youtube Url", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "You must fill the input first", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isValidYouTubeUrl(url: String): Boolean {
        val pattern = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+\$".toRegex()
        return pattern.matches(url)
    }
}