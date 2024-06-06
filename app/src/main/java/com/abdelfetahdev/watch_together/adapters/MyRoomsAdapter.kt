package com.abdelfetahdev.watch_together.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.activities.RoomActivity
import com.abdelfetahdev.watch_together.entities.Room
import com.squareup.picasso.Picasso

class MyRoomsAdapter(private val context: Context, private val dataSet: ArrayList<Room>) :
    RecyclerView.Adapter<MyRoomsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roomCard: CardView
        val roomName: TextView
        val roomImage: ImageView
        val roomCategory: TextView
        val totalRoomMembers: TextView

        init {
            roomCard = view.findViewById(R.id.room_card)
            roomName = view.findViewById(R.id.room_name)
            roomImage = view.findViewById(R.id.room_image)
            roomCategory = view.findViewById(R.id.room_category)
            totalRoomMembers = view.findViewById(R.id.total_room_members)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.room_cardbox, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val room = dataSet[position]

        viewHolder.roomName.text = room.name
        viewHolder.totalRoomMembers.text = (room.totalMembers ?: 0).toString()

        if (room.profileImage != null) {
            Picasso.get().load("${room.profileImage.url}?h=300&w=400&fit=crop&crop=center")
                .placeholder(R.drawable.profile_4_3)
                .into(viewHolder.roomImage)
        } else {
            viewHolder.roomImage.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.profile_4_3,
                    null
                )
            )
        }

        viewHolder.roomCard.setOnClickListener {
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra("room", room.toBundle())
            context.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun insertRooms(rooms: ArrayList<Room>) {
        dataSet.addAll(rooms)
        synchronized(this) {
            notifyDataSetChanged()
        }
    }
}
