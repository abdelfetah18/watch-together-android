package com.abdelfetahdev.watch_together.fragements

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.MyApp
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.activities.CreateRoomActivity
import com.abdelfetahdev.watch_together.activities.SignInActivity
import com.abdelfetahdev.watch_together.adapters.ExploreRoomsAdapter
import com.abdelfetahdev.watch_together.adapters.MyRoomsAdapter
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.rest_api.RestRooms
import com.abdelfetahdev.watch_together.rest_api.RestUsers
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    lateinit var profileImage: ImageView
    lateinit var username: TextView
    lateinit var createNewRoomButton: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        username = view.findViewById(R.id.username)
        profileImage = view.findViewById(R.id.profile_image)
        createNewRoomButton = view.findViewById(R.id.create_new_room_button)

        val myApp = activity?.application as MyApp
        val restUsers = RestUsers(myApp.httpClient)

        lifecycle.coroutineScope.launch {
            val response = restUsers.getCurrentUser()
            if (response.isError) {
                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                if (response.invalidAuth) {
                    context?.startActivity(Intent(context, SignInActivity::class.java))
                }
            } else {
                val user = response.data
                username.text = user!!.username
                if (user.profileImage != null) {
                    Picasso.get().load("${user.profileImage.url}?h=400&w=400&fit=crop&crop=center")
                        .placeholder(R.drawable.profile_4_3)
                        .into(profileImage)
                }
            }
        }

        createNewRoomButton.setOnClickListener {
            startActivity(Intent(context, CreateRoomActivity::class.java))
        }

        val myRoomsAdapter = MyRoomsAdapter(requireContext(), ArrayList<Room>())
        val recyclerView: RecyclerView? = view.findViewById(R.id.my_rooms)
        recyclerView?.adapter = myRoomsAdapter

        val restRooms = RestRooms(myApp.httpClient)

        lifecycle.coroutineScope.launch {
            val response = restRooms.getUserRooms()
            if (response.isError) {
                Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                if (response.invalidAuth) {
                    context?.startActivity(Intent(context, SignInActivity::class.java))
                }
            } else {
                val rooms = response.data
                myRoomsAdapter.insertRooms(rooms ?: ArrayList<Room>())
            }
        }
    }
}