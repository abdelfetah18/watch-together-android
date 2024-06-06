package com.abdelfetahdev.watch_together.fragements

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.activities.SignInActivity
import com.abdelfetahdev.watch_together.adapters.ExploreRoomsAdapter
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    lateinit var profileImage: ImageView
    lateinit var username: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val exploreRoomsAdapter = ExploreRoomsAdapter(requireContext(), ArrayList<Room>())
        val recyclerView: RecyclerView? = view.findViewById(R.id.explore_rooms)
        recyclerView?.adapter = exploreRoomsAdapter

        val myApp = activity?.application as MyApp
        val restRooms = RestRooms(myApp.httpClient)

        profileImage = view.findViewById(R.id.profile_image)
        username = view.findViewById(R.id.username)

        val user = myApp.user
        username.text = user?.username
        if (user?.profileImage != null) {
            Picasso.get().load("${user.profileImage.url}?h=400&w=400&fit=crop&crop=center")
                .placeholder(R.drawable.profile_4_3)
                .into(profileImage)
        }

        lifecycle.coroutineScope.launch {
            val response = restRooms.getExploreRooms()
            if (response.isError) {
                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                if (response.invalidAuth) {
                    context?.startActivity(Intent(context, SignInActivity::class.java))
                }
            } else {
                val rooms = response.data
                exploreRoomsAdapter.insertRooms(rooms ?: ArrayList<Room>())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}