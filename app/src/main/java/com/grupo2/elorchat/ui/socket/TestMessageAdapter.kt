package com.grupo2.elorchat.ui.socket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.databinding.MessageBinding

class TestMessageAdapter : ListAdapter<Message, TestMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

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
            binding.MessagerName.text = message.userId.toString()
            binding.messageText.text = message.message
            binding.messageDate.text = message.createdAt.toString()

        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.message == newItem.message &&
                    oldItem.userId == newItem.userId &&
                    oldItem.chatId == newItem.chatId &&
                    oldItem.createdAt == newItem.createdAt
        }
    }
}