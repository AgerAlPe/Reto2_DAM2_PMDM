package com.grupo2.elorchat.ui.groups.publicgroups

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
import com.grupo2.elorchat.data.preferences.DataStoreManager
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.databinding.FragmentChatsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.ui.groups.GroupsViewModelFactory
import com.grupo2.elorchat.ui.groups.PublicGroupAdapter
import com.grupo2.elorchat.ui.socket.SocketActivity
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PublicGroupsFragment : Fragment() {

    private lateinit var groupListAdapter: PublicGroupAdapter
    private val dataStoreManager by lazy { DataStoreManager.getInstance(ElorChat.context) }
    private val groupRepository = RemoteGroupDataSource()
    private val viewModel: GroupViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //El codigo debe de ir antes de que se devuelva la view
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

        startActivity(intent)
    }

    private fun onJoinButtonClickItem(group: Group) {
        lifecycleScope.launch {
            // Collect the userId value from the Flow
            val userId = dataStoreManager.getSavedUserId().first()

            // Check if userId is not an empty string before parsing
            if (userId != null) {
                // Use parsedUserId in your logic
                //TODO sacar la id del usuario
                viewModel.joinChat(ChatUser(69, group.id,false))
            } else {
                // Handle the case where userId is null
                Log.e("PublicGroupsFragment", "userId is null")
                // Handle the error accordingly
            }
        }
    }
}
