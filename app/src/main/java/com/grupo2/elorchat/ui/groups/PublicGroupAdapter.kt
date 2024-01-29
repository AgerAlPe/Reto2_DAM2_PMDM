package com.grupo2.elorchat.ui.groups

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

    private var userJoinedGroups: Set<Int> = emptySet()

    fun setUserJoinedGroups(joinedGroups: Set<Int>) {
        userJoinedGroups = joinedGroups
        notifyDataSetChanged()
    }

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
        holder.joinButton.setOnClickListener {
            onJoinButtonClickListener(group)
        }

        // Check if the user is already in the group and hide the button
        if (userJoinedGroups.contains(group.id)) {
            holder.joinButton.visibility = View.GONE
        } else {
            holder.joinButton.visibility = View.VISIBLE
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
