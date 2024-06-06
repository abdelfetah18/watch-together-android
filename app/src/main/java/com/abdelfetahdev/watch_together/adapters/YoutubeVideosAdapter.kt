package com.abdelfetahdev.watch_together.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.activities.VideoPlayerActivity
import com.abdelfetahdev.watch_together.entities.Member
import com.abdelfetahdev.watch_together.entities.Room
import com.abdelfetahdev.watch_together.entities.YoutubeVideo
import com.squareup.picasso.Picasso

class YoutubeVideosAdapter(
    private val context: Context,
    private val room: Room,
    private val dataSet: ArrayList<YoutubeVideo>
) :
    RecyclerView.Adapter<YoutubeVideosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val videoWrapper: LinearLayout
        val thumbnail: ImageView
        val videoTitle: TextView
        val channelName: TextView
        val views: TextView
        val timestamp: TextView
        val postedAt: TextView


        init {
            videoWrapper = view.findViewById(R.id.video_wrapper)
            thumbnail = view.findViewById(R.id.thumbnail)
            videoTitle = view.findViewById(R.id.video_title)
            channelName = view.findViewById(R.id.channel_name)
            views = view.findViewById(R.id.views_count)
            timestamp = view.findViewById(R.id.timestamp)
            postedAt = view.findViewById(R.id.video_posted_at)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.youtube_video_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val video = dataSet[position]

        viewHolder.videoTitle.text = video.videoTitle
        viewHolder.views.text = video.views
        viewHolder.timestamp.text = video.timestamp
        viewHolder.channelName.text = video.channelName
        viewHolder.postedAt.text = video.postedAt

        Picasso.get().load(video.thumbnailUrl).placeholder(R.drawable.profile_4_3)
            .into(viewHolder.thumbnail)

        viewHolder.videoWrapper.setOnClickListener {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra("videoUrl", video.videoUrl)
            intent.putExtra("room", room.toBundle())
            context.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun insertYoutubeVideos(videos: ArrayList<YoutubeVideo>) {
        dataSet.addAll(videos)
        synchronized(this) {
            notifyDataSetChanged()
        }
    }
}
