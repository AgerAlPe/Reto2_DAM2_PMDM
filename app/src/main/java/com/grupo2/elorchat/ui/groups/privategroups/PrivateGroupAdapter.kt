package com.grupo2.elorchat.ui.groups.privategroups

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.ChatUserEmailRequest
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.databinding.ItemGroupBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel

class PrivateGroupAdapter(
    private val onClickListener: (Group) -> Unit,
    private val viewModel: GroupViewModel
) : ListAdapter<Group, PrivateGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
        holder.itemView.setOnClickListener {
            onClickListener(group)
        }
    }

    inner class GroupViewHolder(
        val binding: ItemGroupBinding,
        private val viewModel: GroupViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group) {
            val isAdminGroup = viewModel.adminInGroups.value?.any { it.id == group.id } ?: false

            binding.GroupName.text = group.name

            if (isAdminGroup) {
                setupAdminUI(group)
            } else {
                binding.joinButton.visibility = View.GONE
            }
        }

        private fun setupAdminUI(group: Group) {
            binding.joinButton.setImageResource(R.drawable.baseline_settings_24)
            binding.joinButton.setOnClickListener { view ->
                Log.i("PruebaBottonJoin", "se ha pulsado el boton")
                showPopupMenu(view, group)
            }
            binding.joinButton.visibility = View.VISIBLE
        }

        private fun showPopupMenu(view: View, group: Group) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.group_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_add_user -> {
                        showUserDialog(view.context, group, "Add User") { email ->
                            viewModel.makeAnUserJoin(ChatUserEmailRequest(email, group.id))
                        }
                        true
                    }
                    R.id.menu_kick_user -> {
                        showUserDialog(view.context, group, "Kick User") { email ->
                            viewModel.makeAnUserLeave(ChatUserEmailRequest(email, group.id))
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun showUserDialog(
            context: Context,
            group: Group,
            title: String,
            action: (String) -> Unit
        ) {
            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_user, null)
            builder.setView(dialogView)
                .setTitle(title)
                .setPositiveButton(title) { _, _ ->
                    val emailEditText = dialogView.findViewById<EditText>(R.id.editTextEmail)
                    val email = emailEditText.text.toString()
                    if (email.isNotBlank()) {
                        action(email)
                    } else {
                        // Handle invalid email input (e.g., show a message)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
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
}