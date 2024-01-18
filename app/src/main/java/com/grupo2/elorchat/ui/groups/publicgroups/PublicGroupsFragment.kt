package com.grupo2.elorchat.ui.groups.publicgroups

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.databinding.FragmentPrivateChatsBinding
import com.grupo2.elorchat.ui.groups.GroupAdapter
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.ui.groups.GroupsViewModelFactory
import com.grupo2.elorchat.ui.users.register.RegisterActivity
import com.grupo2.elorchat.utils.Resource

class PublicGroupsFragment : Fragment() {

    private lateinit var groupListAdapter: GroupAdapter
    private val groupRepository = RemoteGroupDataSource()
    private val viewModel: GroupViewModel by viewModels { GroupsViewModelFactory(groupRepository) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //El codigo debe de ir antes de que se devuelva la view
        val binding = FragmentPrivateChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        groupListAdapter = GroupAdapter(::onGroupsListClickItem)
        binding.privateGroups.adapter = groupListAdapter
        binding.privateGroups.layoutManager = LinearLayoutManager(requireContext())

        viewModel.publicGroups.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        if (it.data != null) {
                            //SE DEBERÍAN MOSTRAR LOS GRUPOS PRIVADOS
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
        val intent = Intent(requireContext(), RegisterActivity::class.java).apply {
            putExtra("idGroup", group.id)
        }
        startActivity(intent)
    }
}