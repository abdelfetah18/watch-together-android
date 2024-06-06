package com.abdelfetahdev.watch_together.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.adapters.RoomMembersAdapter
import com.abdelfetahdev.watch_together.adapters.YoutubeVideosAdapter
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import com.abdelfetahdev.watch_together.utilities.LoadingDialogue
import kotlinx.coroutines.launch

class SearchYoutubeVideosActivity : AppCompatActivity() {
    lateinit var queryInput: EditText
    lateinit var searchButton: ImageView
    lateinit var loadingDialogue: LoadingDialogue
    lateinit var goBackButton: ImageView
    lateinit var room: Room
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_youtube_videos)

        loadingDialogue = LoadingDialogue(this@SearchYoutubeVideosActivity)
        val bundle = intent.getBundleExtra("room")
        if (bundle != null) {
            room = Room.fromBundle(bundle)
        }

        val query = intent.extras?.getString("query")
        if (query != null) {
            searchYoutube(query)
        }

        queryInput = findViewById(R.id.query_input)
        searchButton = findViewById(R.id.search_button)
        goBackButton = findViewById(R.id.go_back_button)

        searchButton.setOnClickListener {
            searchYoutube(queryInput.text.toString())
        }

        goBackButton.setOnClickListener { finish() }

        queryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchYoutube(queryInput.text.toString())
            }
            true
        }
    }

    private fun searchYoutube(query: String) {
        loadingDialogue.show()
        val myApp = application as MyApp
        val youtubeVideosAdapter =
            YoutubeVideosAdapter(this@SearchYoutubeVideosActivity, room, ArrayList())
        val recyclerView: RecyclerView = findViewById(R.id.youtube_search_result)
        recyclerView.adapter = youtubeVideosAdapter

        val restRooms = RestRooms(myApp.httpClient)

        lifecycle.coroutineScope.launch {
            val response = restRooms.searchYoutubeVideos(query)
            loadingDialogue.hide()
            if (response.isError) {
                Toast.makeText(
                    this@SearchYoutubeVideosActivity,
                    response.message,
                    Toast.LENGTH_LONG
                ).show()
                if (response.invalidAuth) {
                    startActivity(
                        Intent(
                            this@SearchYoutubeVideosActivity,
                            SignInActivity::class.java
                        )
                    )
                }
            } else {
                val videos = response.data
                youtubeVideosAdapter.insertYoutubeVideos(videos ?: ArrayList())
            }
        }
    }
}