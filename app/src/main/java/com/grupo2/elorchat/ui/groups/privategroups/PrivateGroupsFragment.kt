package com.grupo2.elorchat.ui.groups.privategroups

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.text.Editable
import android.text.TextWatcher
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
import com.grupo2.elorchat.data.ChatUserMovementResponse
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
    private val viewModel: GroupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root
        val searchGroup = binding.searchGroup

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

        viewModel.addUser.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                handleUserMovementAdd(response)
            }
        }

        viewModel.kickUser.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                handleUserMovementLeave(response)
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

    private fun handleUserMovementAdd(response: ChatUserMovementResponse) {
        response.response.let { Log.i("respuesta", it) }
        when (response.response) {
            "User Joined Chat" -> showToast("Usuario se ha añadido con exito")
            // Add more conditions as needed
        else -> showToast("error al añadir usuario")
        }
    }

    private fun handleUserMovementLeave(response: ChatUserMovementResponse) {
        response.response.let { Log.i("respuesta", it) }
        when (response.response) {
            "End of input at line 1 column 1 path \$" -> showToast("Usuario se ha kickeado con exito")
            // Add more conditions as needed
        else -> showToast("error al eliminar usuario")
        }
    }

    private fun showToast(message: String) {
        // Show a Toast with the provided message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}