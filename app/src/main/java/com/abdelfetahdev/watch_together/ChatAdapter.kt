package com.abdelfetahdev.watch_together

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ChatAdapter(private val user_id: String, private val data: MutableList<Message>) : RecyclerView.Adapter<ChatViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ChatViewHolder {
        val layoutResId = if (viewType == 0) {
            R.layout.message_sent
        } else {
            R.layout.message_recv
        }

        return ChatViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].user._id == user_id) 0 else 1
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = data[position]
        holder.message.text = message.message
        holder.date.text = message._createdAt

        // Load Image
        val imageURL = message.user.profile_image
        if (imageURL != "null") {
            Picasso.get().load(imageURL).placeholder(R.drawable.profile_1_1).into(holder.userImage)
        }
    }

    override fun onViewRecycled(holder: ChatViewHolder) {
        super.onViewRecycled(holder)
        Picasso.get().cancelRequest(holder.userImage)
        holder.userImage.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.profile_1_1))
    }
}


class ChatViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val userImage : ImageView = itemView.findViewById(R.id.user_image)
    val message : TextView = itemView.findViewById(R.id.message)
    val date : TextView = itemView.findViewById(R.id.date)
}