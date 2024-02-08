package com.grupo2.elorchat.ui.groups.publicgroups

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.databinding.ItemGroupBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel

class PublicGroupAdapter(
    private val onGroupClickListener: (Group) -> Unit,
    private val onJoinButtonClickListener: (Group) -> Unit,
    private val viewModel: GroupViewModel

) : ListAdapter<Group, PublicGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
        holder.itemView.setOnClickListener {
            onGroupClickListener(group)
        }

    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteButton.setOnClickListener {
                val group = getItem(adapterPosition)
                viewModel.deleteGroup(group.id)
            }
        }

        fun bind(group: Group) {
            binding.GroupName.text = group.name
            binding.joinButton.setImageResource(R.drawable.baseline_person_add_24)
            val isAdminGroup = viewModel.adminInGroups.value?.any { it.id == group.id } ?: false

            // Show or hide the delete button based on whether the user is an admin
            binding.deleteButton.visibility = if (isAdminGroup) View.VISIBLE else View.GONE
            binding.deleteButton.setOnClickListener {
                // Handle delete button click here
                // You can call a function in your ViewModel to handle group deletion
                viewModel.deleteGroup(group.id)
            }

            // Log the values for debugging
            Log.d(
                "PublicGroupAdapter",
                "Group ID: ${group.id}, Name: ${group.name}, isUserOnGroup: ${group.isUserOnGroup}"
            )

            // Check if the user is already in the group
            binding.joinButton.visibility = if (group.isUserOnGroup) View.GONE else View.VISIBLE
            binding.joinButton.setOnClickListener {
                group.isUserOnGroup = true
                binding.joinButton.visibility = View.GONE
                onJoinButtonClickListener(group)
            }
        }
    }


    // Method to update the dataset
    fun updateGroups(groups: List<Group>) {
        submitList(groups)
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {

        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return (oldItem.id == newItem.id && oldItem.name == newItem.name)
        }
    }
}