package com.abdelfetahdev.watch_together.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.Member
import com.squareup.picasso.Picasso

class RoomMembersAdapter(private val context: Context, private val dataSet: ArrayList<Member>) :
    RecyclerView.Adapter<RoomMembersAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView
        val username: TextView
        val joinedAt: TextView


        init {
            profileImage = view.findViewById(R.id.profile_image)
            username = view.findViewById(R.id.username)
            joinedAt = view.findViewById(R.id.joined_at)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.member_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val member = dataSet[position]

        viewHolder.username.text = member.user.username

        if (member.user.profileImage != null) {
            Picasso.get().load("${member.user.profileImage.url}?h=400&w=400&fit=crop&crop=center")
                .placeholder(
                    R.drawable.profile_4_3
                )
                .into(viewHolder.profileImage)
        } else {
            viewHolder.profileImage.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.profile_1_1,
                    null
                )
            )
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun insertMembers(members: ArrayList<Member>) {
        dataSet.addAll(members)
        synchronized(this) {
            notifyDataSetChanged()
        }
    }
}
