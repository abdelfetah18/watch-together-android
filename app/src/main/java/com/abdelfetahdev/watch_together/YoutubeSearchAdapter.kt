package com.abdelfetahdev.watch_together

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class YoutubeSearchAdapter(private val roomId: String, private val isAdmin: Boolean, private val data: MutableList<Video>) : RecyclerView.Adapter<YoutubeSearchViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : YoutubeSearchViewHolder {
        return YoutubeSearchViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.youtube_video_carbox, parent, false)
        )
    }

    override fun onBindViewHolder(holder: YoutubeSearchViewHolder, position: Int) {
        val video = data[position]
        holder.videoTitle.text = video.getTitle()

        // Load Image
        val imageURL = video.getThumb()
        if (imageURL != "null") {
            println("${video.getTitle()} -> $imageURL")
            Picasso.get().load(imageURL).placeholder(R.drawable.profile_4_3).into(holder.videoImage)
        }

        holder.watchBtn.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, YoutubePlayerActivity::class.java)
            val bundle = Bundle()
            bundle.putString("videoUrl", video.getUrl())
            bundle.putString("roomId", roomId)
            bundle.putBoolean("isAdmin", isAdmin)

            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun onViewRecycled(holder: YoutubeSearchViewHolder) {
        super.onViewRecycled(holder)
        Picasso.get().cancelRequest(holder.videoImage)
        holder.videoImage.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.profile_4_3))
    }
}


class YoutubeSearchViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val videoTitle : TextView = itemView.findViewById(R.id.video_title)
    val videoImage : ImageView = itemView.findViewById(R.id.video_image)
    val watchBtn : Button = itemView.findViewById(R.id.watch_btn)
}