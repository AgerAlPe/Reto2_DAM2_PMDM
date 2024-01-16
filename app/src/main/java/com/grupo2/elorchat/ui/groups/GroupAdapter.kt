package com.grupo2.elorchat.ui.groups

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.databinding.GroupBinding

class GroupAdapter(
    private val onClickListener: (Group) -> Unit,
    private val onImageButtonClick: (Group) -> Unit // Define a new click listener for the ImageButton
) : ListAdapter<Group, GroupAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
        holder.itemView.setOnClickListener {
            onClickListener(song)
        }
    }

    inner class SongViewHolder(private val binding: GroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group) {
            binding.GroupName.text = group.name

            binding.itemButton.setOnClickListener {
                onImageButtonClick(group) // Call the provided function
                Log.i("mikel","1")

            }
            if (group.private) {
                //TODO
                //CAMBIAR ESTO PARA QUE, DEPENDIENDO DESDE DONDE SE LE LLAME, SE MUESTRE EL GRUPO
                group.private == true
            }else {
                group.private == false
            }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Group>() {

        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return (oldItem.id == newItem.id && oldItem.name == newItem.name)
        }
    }
}