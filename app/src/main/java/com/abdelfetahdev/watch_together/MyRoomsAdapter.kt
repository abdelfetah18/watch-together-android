package com.abdelfetahdev.watch_together

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MyRoomsAdapter(private val data: MutableList<Room>) : RecyclerView.Adapter<MyRoomsViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyRoomsViewHolder {
        return MyRoomsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.my_room_cardbox, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyRoomsViewHolder, position: Int) {
        val room = data[position]
        holder.roomName.text = room.name
        holder.totalMembers.text = "${room.total_members} members"

        // Load Image
        val imageURL = room.profile_image
        if (imageURL != "null") {
            println("${room.name} -> $imageURL")
            Picasso.get().load(imageURL).placeholder(R.drawable.profile_4_3).into(holder.roomImage)
        }

        // Adding on click listener
        holder.openBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, RoomActivity::class.java)
            val bundle = Bundle()
            bundle.putString("room_id", room._id)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun onViewRecycled(holder: MyRoomsViewHolder) {
        super.onViewRecycled(holder)
        Picasso.get().cancelRequest(holder.roomImage)
        holder.roomImage.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.profile_4_3))
    }
}


class MyRoomsViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val roomName : TextView = itemView.findViewById(R.id.room_name)
    val roomImage : ImageView = itemView.findViewById(R.id.room_image)
    val totalMembers : TextView = itemView.findViewById(R.id.total_members)
    val openBtn : Button = itemView.findViewById(R.id.open_room_btn)
}