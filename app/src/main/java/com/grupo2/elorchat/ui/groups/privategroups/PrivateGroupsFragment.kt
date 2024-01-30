package com.grupo2.elorchat.ui.groups.privategroups

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.ChatUserEmailRequest
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.databinding.FragmentChatsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.ui.groups.PrivateGroupAdapter
import com.grupo2.elorchat.ui.socket.SocketActivity
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivateGroupsFragment : Fragment() {

    private lateinit var groupListAdapter: PrivateGroupAdapter
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var emailEditText: EditText
    private val viewModel: GroupViewModel by viewModels()
    private var selectedGroup: Group? = null
    private var selectedAnchorView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        groupListAdapter = PrivateGroupAdapter(
            ::onGroupsListClickItem,
            viewModel
        )
        binding.groupsList.adapter = groupListAdapter
        binding.groupsList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.privateGroups.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let { privateGroups ->
                            groupListAdapter.submitList(privateGroups)
                        }
                    }

                    Resource.Status.ERROR -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }

                    Resource.Status.LOADING -> {
                        // Handle loading state if needed
                    }
                }
            }
        }

        emailEditText = EditText(requireContext())
        emailEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailEditText.hint = "Email"

        dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Enter Email")
            .setView(emailEditText)
            .setPositiveButton("OK") { dialog, _ ->
                val userEmail = emailEditText.text.toString()

                if (isValidEmail(userEmail)) {
                    selectedGroup?.let { group ->
                        selectedAnchorView?.let { anchorView ->
                            handleEmailAction(group, anchorView.id, userEmail)
                        }
                    }
                } else {
                    // Show an error or prompt the user for a valid email
                    Toast.makeText(requireContext(), "Invalid Email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle cancel action if needed
            }

        return view
    }

    private fun onGroupsListClickItem(group: Group) {
        val intent = Intent(requireContext(), SocketActivity::class.java).apply {
            putExtra("idGroup", group.id.toString())
        }
        startActivity(intent)
    }

    private fun handleEmailAction(group: Group, actionId: Int, userEmail: String) {
        viewModel.handleEmailAction(group, actionId, userEmail)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}