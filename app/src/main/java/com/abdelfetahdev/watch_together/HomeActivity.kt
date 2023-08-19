package com.abdelfetahdev.watch_together

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import okhttp3.internal.notify
import okhttp3.internal.notifyAll


class HomeActivity : AppCompatActivity() {
    private lateinit var accessToken: String
    private var rooms = mutableListOf<Room>()
    private lateinit var listView : RecyclerView
    private lateinit var adapter : ExploreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val myApplication: MyApp = application as MyApp
        val store = myApplication.getMyUserStore()
        accessToken = store.getToken().toString()

        val navBar = NavigationController(findViewById(R.id.home_btn), findViewById(R.id.profile_btn), findViewById(R.id.settings_btn), this)
        navBar.initNavigation("home")

        listView = findViewById(R.id.explore_rooms)

        listView.layoutManager = LinearLayoutManager(this)

        adapter = ExploreAdapter(application as MyApp,rooms)
        listView.adapter = adapter

        getExploreRooms()

        val searchBar = findViewById<EditText>(R.id.search_bar)
        searchBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val query: String = searchBar.text.toString()
                searchRoom(query)
                searchBar.text.clear()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun getExploreRooms(){
        runBlocking {
            rooms.addAll((application as MyApp).client.getExploreRooms())
        }
    }

    private fun searchRoom(query: String){
        runBlocking {
            rooms.clear()
            rooms.addAll((application as MyApp).client.searchRooms(query))
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}