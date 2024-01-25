package com.grupo2.elorchat.ui.socket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.SocketMessage
import com.grupo2.elorchat.databinding.ItemMessageBinding
import com.grupo2.elorchat.databinding.MessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(private val binding: MessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.messageText.text = message.message
            binding.MessagerName.text = message.name
            val dateString = convertTimestampToDate(message.createdAt)
            binding.messageDate.text = dateString
            // You can bind other properties as needed
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id // Assuming Message has an 'id' property
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.message == newItem.message &&
                    oldItem.name == newItem.name &&
                    oldItem.userId == newItem.userId &&
                    oldItem.chatId == newItem.chatId &&
                    oldItem.createdAt == newItem.createdAt
            // Add other properties as needed
        }
    }

    fun convertTimestampToDate(timestamp: Long?): String {
        if (timestamp == null) {
            return ""
        }

        try {
            val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            val netDate = Date(timestamp)
            return sdf.format(netDate)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
