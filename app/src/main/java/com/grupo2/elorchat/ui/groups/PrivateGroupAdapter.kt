package com.grupo2.elorchat.ui.groups

import android.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ActionMenuView.OnMenuItemClickListener
import android.widget.EditText
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.databinding.GroupBinding

class PrivateGroupAdapter(
    private val onClickListener: (Group) -> Unit,
    private val viewModel: GroupViewModel
) : ListAdapter<Group, PrivateGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
        holder.itemView.setOnClickListener {
            onClickListener(group)
        }
        holder.binding.joinButton.setOnClickListener { view ->
            holder.showPopupMenu(group, view)
        }
    }

    inner class GroupViewHolder(
        val binding: GroupBinding,
        private val viewModel: GroupViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group) {
            val isAdminGroup = viewModel.adminInGroups.value?.any { it.id == group.id } ?: false

            binding.GroupName.text = group.name

            if (isAdminGroup) {
                binding.joinButton.setImageResource(R.drawable.baseline_settings_24)
                binding.joinButton.setOnClickListener { view ->
                    showPopupMenu(group, view)
                }
                binding.joinButton.visibility = View.VISIBLE
            } else {
                binding.joinButton.visibility = View.GONE
            }
        }

        fun showPopupMenu(group: Group, anchorView: View) {
            val popupMenu = PopupMenu(itemView.context, anchorView)
            popupMenu.menuInflater.inflate(R.menu.group_menu, popupMenu.menu)

            val emailEditText = EditText(itemView.context)
            emailEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            emailEditText.hint = "Email"

            val dialogBuilder = AlertDialog.Builder(itemView.context)
                .setTitle("Enter Email")
                .setView(emailEditText)
                .setPositiveButton("OK") { dialog, _ ->
                    val userEmail = emailEditText.text.toString()
                    handleEmailAction(group, anchorView.id, userEmail)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle cancel action if needed
                }

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_add_user, R.id.menu_kick_user -> {
                        dialogBuilder.show()
                        true
                    }
                    R.id.menu_purge -> {
                        // Handle purge action (no email required for purge)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        private fun handleEmailAction(group: Group, actionId: Int, userEmail: String) {
            viewModel.handleEmailAction(group, actionId, userEmail)
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