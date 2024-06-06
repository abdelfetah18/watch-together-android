package com.abdelfetahdev.watch_together.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdelfetahdev.watch_together.R
import com.abdelfetahdev.watch_together.entities.Message
import com.squareup.picasso.Picasso

class ChatMessagesAdapter(
    private val context: Context,
    private val userId: String,
    private val dataSet: ArrayList<Message>
) :
    RecyclerView.Adapter<ChatMessagesAdapter.BaseViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position].user?.id == userId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView
        val date: TextView

        init {
            message = view.findViewById(R.id.message)
            date = view.findViewById(R.id.date)
        }
    }

    class RecvMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView
        val username: TextView
        val profileImage: ImageView

        init {
            message = view.findViewById(R.id.message)
            username = view.findViewById(R.id.username)
            profileImage = view.findViewById(R.id.profile_image)
        }
    }

    class BaseViewHolder(view: View, val isSend: Boolean) : RecyclerView.ViewHolder(view) {
        val viewHolder: RecyclerView.ViewHolder

        init {
            viewHolder = if (isSend) SentMessageViewHolder(view) else RecvMessageViewHolder(view)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseViewHolder {
        // Create a new view, which defines the UI of the list item
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.message_sent, viewGroup, false)
            BaseViewHolder(view, true)
        } else {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.message_recv, viewGroup, false)
            BaseViewHolder(view, false)
        }
    }

    override fun onBindViewHolder(viewHolder: BaseViewHolder, position: Int) {
        val message = dataSet[position]

        if (viewHolder.isSend) {
            val sentMessageViewHolder = viewHolder.viewHolder as SentMessageViewHolder
            sentMessageViewHolder.message.text = message.message
        } else {
            val recvMessageViewHolder = viewHolder.viewHolder as RecvMessageViewHolder
            recvMessageViewHolder.message.text = message.message
            recvMessageViewHolder.username.text = message.user?.username

            if (message.user?.profileImage != null) {
                Picasso.get().load(message.user.profileImage.url)
                    .placeholder(R.drawable.profile_1_1)
                    .into(recvMessageViewHolder.profileImage)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun insertMessages(messages: ArrayList<Message>) {
        dataSet.addAll(messages)
        synchronized(this) {
            notifyDataSetChanged()
        }
    }

    fun insertMessage(message: Message) {
        dataSet.add(message)
        synchronized(this) {
            notifyDataSetChanged()
        }
    }
}
