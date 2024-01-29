package com.grupo2.elorchat.ui.groups.privategroups

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo2.elorchat.R
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
    private val groupRepository = RemoteGroupDataSource()
    private val viewModel: GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        groupListAdapter = PrivateGroupAdapter(
            ::onGroupsListClickItem
        ) { group, anchorView, userEmail -> onMenuClick(group, anchorView, userEmail) }
        binding.groupsList.adapter = groupListAdapter
        binding.groupsList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.privateGroups.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        if (it.data != null) {
                            groupListAdapter.submitList(it.data)
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

        return view
    }

    private fun onMenuClick(group: Group, anchorView: View, userEmail: String?) {
        when (anchorView.id) {
            R.id.menu_add_user -> {
                // Handle add user action with userEmail
                if (!userEmail.isNullOrEmpty()) {
                    // Perform the add user action using userEmail
                } else {
                    // Show an error or prompt the user to enter an email
                }
            }
            R.id.menu_kick_user -> {
                // Handle kick user action with userEmail
                if (!userEmail.isNullOrEmpty()) {
                    // Perform the kick user action using userEmail
                } else {
                    // Show an error or prompt the user to enter an email
                }
            }
            R.id.menu_purge -> {
                // Handle purge action (no email required for purge)
            }
            else -> {
                // Handle unexpected case
            }
        }
    }

    private fun onGroupsListClickItem(group: Group) {
        val intent = Intent(requireContext(), SocketActivity::class.java).apply {
            putExtra("idGroup", group.id.toString())
        }
        startActivity(intent)
    }
}


