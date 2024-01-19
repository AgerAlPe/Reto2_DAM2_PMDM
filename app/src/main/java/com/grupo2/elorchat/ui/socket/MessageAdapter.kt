package com.grupo2.elorchat.ui.socket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.data.SocketMessage
import com.grupo2.elorchat.databinding.ItemMessageBinding

class MessageAdapter() : ListAdapter<SocketMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(socketMessage: SocketMessage) {
            binding.textViewMessage.text = socketMessage.text
            binding.textViewAuthor.text = socketMessage.authorName
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<SocketMessage>() {

        override fun areItemsTheSame(oldItem: SocketMessage, newItem: SocketMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SocketMessage, newItem: SocketMessage): Boolean {
            return oldItem.room == newItem.room && oldItem.text == newItem.text && oldItem.authorName == newItem.authorName
        }
    }
}