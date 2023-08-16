package com.abdelfetahdev.watch_together

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking

class ExploreAdapter(private val application: MyApp, private val data: MutableList<Room>) : RecyclerView.Adapter<ExploreViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ExploreViewHolder {
        return ExploreViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.room_cardbox, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        val room = data[position]
        holder.roomName.text = room.name
        holder.totalMembers.text = "${room.total_members} members"

        // Load Image
        val imageURL = room.profile_image
        if (imageURL != "null") {
            println("${room.name} -> $imageURL")
            Picasso.get().load(imageURL).placeholder(R.drawable.profile_4_3).into(holder.roomImage)
        }

        holder.joinBtn.setOnClickListener {
            holder.joinBtn.visibility = View.GONE
            holder.loadingBtn.visibility = View.VISIBLE

            holder.initAnimator()
            holder.animator?.start()

            runBlocking {
                val didJoin = application.client.joinRoom(room._id)
                if(didJoin){
                   data.removeAt(position)
                }else{
                    Toast.makeText(application.baseContext, "Room join failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewRecycled(holder: ExploreViewHolder) {
        super.onViewRecycled(holder)
        Picasso.get().cancelRequest(holder.roomImage)
        holder.roomImage.setImageDrawable(holder.itemView.context.getDrawable(R.drawable.profile_4_3))

        holder.joinBtn.visibility = View.VISIBLE
        holder.loadingBtn.visibility = View.GONE
        if(holder.animator != null){
            holder.animator?.cancel()
        }
    }
}


class ExploreViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    val roomName : TextView = itemView.findViewById(R.id.room_name)
    val roomImage : ImageView = itemView.findViewById(R.id.room_image)
    val totalMembers : TextView = itemView.findViewById(R.id.total_members)
    val loadingBtn : ImageButton = itemView.findViewById(R.id.loading_btn)
    val joinBtn : Button = itemView.findViewById(R.id.join_room_btn)
    var animator : ValueAnimator? = null

    fun initAnimator(){
        // Create a ValueAnimator for the rotation animation
        animator = ValueAnimator.ofFloat(0f, 360f)
        animator!!.startDelay = 1000
        animator!!.duration = 1000
        animator!!.interpolator = LinearInterpolator()
        animator!!.repeatCount = ValueAnimator.INFINITE

        // Set up the AnimatorListener
        animator!!.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            loadingBtn.rotation = animatedValue
        }
    }
}