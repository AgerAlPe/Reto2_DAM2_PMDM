package com.grupo2.elorchat.ui.groups.privategroups

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.databinding.FragmentChatsBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
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
        val searchGroup = binding.searchGroup

        groupListAdapter = PrivateGroupAdapter( ::onGroupsListClickItem)
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

        searchGroup.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterPrivateGroupsByName(s.toString())
            }

        })

        return view
    }

    private fun onGroupsListClickItem(group: Group) {
        val intent = Intent(requireContext(), SocketActivity::class.java).apply {
            putExtra("idGroup", group.id.toString())
        }
        startActivity(intent)
    }
}


