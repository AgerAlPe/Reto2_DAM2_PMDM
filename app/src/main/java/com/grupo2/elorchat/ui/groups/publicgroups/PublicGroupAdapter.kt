package com.grupo2.elorchat.ui.groups.publicgroups

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.databinding.GroupBinding

class PublicGroupAdapter(
    private val onGroupClickListener: (Group) -> Unit,
    private val onJoinButtonClickListener: (Group, Boolean) -> Unit
) : ListAdapter<Group, PublicGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
        holder.itemView.setOnClickListener {
            onGroupClickListener(group)
        }

        // Check if the user is already in the group
        if (group.isUserOnGroup) {
            holder.setLeaveButtonState(group)
        } else {
            holder.setJoinButtonState(group)
        }
    }

    inner class GroupViewHolder(private val binding: GroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val joinButton = binding.joinButton

        fun bind(group: Group) {
            binding.GroupName.text = group.name

            // Set the initial state of the joinButton
            joinButton.text = if (group.isUserOnGroup) "Leave" else "Join"

            // Update the joinButton text based on the isUserOnGroup property
            joinButton.setOnClickListener {
                it.isEnabled = false // Disable the button temporarily

                if (group.isUserOnGroup) {
                    group.isUserOnGroup = false
                    setJoinButtonState(group)
                    onJoinButtonClickListener(group, false)
                } else {
                    group.isUserOnGroup = true
                    setLeaveButtonState(group)
                    onJoinButtonClickListener(group, true)
                }

                Handler().postDelayed({
                    it.isEnabled = true // Re-enable the button after a delay
                }, 500) // You can adjust the delay time as needed
            }
        }

        fun setJoinButtonState(group: Group) {
            joinButton.text = "Join"
        }

        fun setLeaveButtonState(group: Group) {
            joinButton.text = "Leave"
        }
    }
}

class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {

    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return (oldItem.id == newItem.id && oldItem.name == newItem.name)
    }
}