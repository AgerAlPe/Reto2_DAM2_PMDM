package com.grupo2.elorchat.ui.groups

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
    private val onJoinButtonClickListener: (Group) -> Unit
) : ListAdapter<Group, PublicGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    private var joinedGroupId: Int? = null

    fun setGroupJoined(groupId: Int) {
        joinedGroupId = groupId
        notifyDataSetChanged() // Update the UI
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
        holder.itemView.setOnClickListener {
            onGroupClickListener(group)
        }

        // Log the values for debugging
        Log.d("GroupAdapter", "Group ID: ${group.id}, Name: ${group.name}, isUserOnGroup: ${group.isUserOnGroup}")

        // Check if the user is already in the group
        if (group.isUserOnGroup) {
            Log.d("GroupAdapter", "User is already in the group, hiding joinButton")
            holder.joinButton.visibility = View.GONE
        } else {
            Log.d("GroupAdapter", "User is not in the group, showing joinButton")
            holder.joinButton.visibility = View.VISIBLE
            holder.joinButton.setOnClickListener {
                onJoinButtonClickListener(group)
            }
        }
    }

    inner class GroupViewHolder(private val binding: GroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val joinButton = binding.joinButton

        fun bind(group: Group) {
            binding.GroupName.text = group.name
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
