package com.grupo2.elorchat.ui.groups.publicgroups

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.data.repository.remote.RemoteSocketDataSource
import com.grupo2.elorchat.databinding.FragmentChatsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.ui.groups.PublicGroupAdapter
import com.grupo2.elorchat.ui.socket.SocketActivity
import com.grupo2.elorchat.ui.socket.SocketViewModel
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PublicGroupsFragment(private val appDatabase: AppDatabase) : Fragment() {

    private lateinit var groupListAdapter: PublicGroupAdapter
    private val dataStoreManager by lazy { DataStoreManager.getInstance(ElorChat.context) }
    private val groupRepository = RemoteGroupDataSource()
    private val socketRepository = RemoteSocketDataSource()
    private val viewModel: GroupViewModel by viewModels()
    private val socketViewModel : SocketViewModel by viewModels()
    private val SOCKET_ACTIVITY_REQUEST_CODE = 123

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        groupListAdapter = PublicGroupAdapter(
            onGroupClickListener = ::onGroupsListClickItem,
            onJoinButtonClickListener = ::onJoinButtonClickItem
        )
        binding.groupsList.adapter = groupListAdapter
        binding.groupsList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.publicGroups.observe(viewLifecycleOwner, Observer {
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
        })

        return view
    }

    private fun onGroupsListClickItem(group: Group) {
        val intent = Intent(requireContext(), SocketActivity::class.java).apply {
            putExtra("idGroup", group.id.toString())
            putExtra("groupName", group.name)
        }

        startActivityForResult(intent, SOCKET_ACTIVITY_REQUEST_CODE)
    }

    private fun refreshFragmentData() {
        // Your logic to refresh the fragment data
        viewModel.updateGroupList()
        // Add any other logic needed to refresh the entire view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SOCKET_ACTIVITY_REQUEST_CODE) {
            // Check if the result is OK and perform the refresh/update action
            if (resultCode == Activity.RESULT_OK) {
                // Refresh the fragment data
                refreshFragmentData()
            }
        }
    }

    private fun onJoinButtonClickItem(group: Group) {
        lifecycleScope.launch {
            try {
                val userId = appDatabase.getUserDao().getAllUser().firstOrNull()?.id

                if (userId != null) {
                    viewModel.joinChat(ChatUser(userId, group.id, false))

                    socketViewModel.joinRoom(group.name, userId)

                    viewModel.joinChat.observe(this@PublicGroupsFragment, Observer { result ->
                        when (result.status) {
                            Resource.Status.SUCCESS -> {
                                Toast.makeText(requireContext(), "Successfully joined the chat", Toast.LENGTH_SHORT).show()
                                viewModel.updateGroupList()
                                // You can add any additional actions on success if needed
                            }
                            Resource.Status.ERROR -> {
                                Toast.makeText(requireContext(), "Error joining the chat: ${result.message}", Toast.LENGTH_SHORT).show()
                                // Handle error state, if needed
                            }
                            Resource.Status.LOADING -> {
                                // Handle loading state if needed
                            }
                        }
                    })
                } else {
                    Log.e("YourActivity", "userId is null")
                    // Handle the case where userId is null
                }
            } catch (e: Exception) {
                Log.e("YourActivity", "Exception while getting user ID: ${e.message}")
                // Handle exceptions if any
            }
        }
    }
}