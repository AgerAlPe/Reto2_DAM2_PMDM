package com.grupo2.elorchat.ui.socket

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.repository.MessageRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.databinding.ActivitySocketBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SocketActivity() : ComponentActivity() {

    private val TAG = "SocketActivity"
    private var groupId: Int? = null
    private lateinit var messageAdapter: MessageAdapter
    private val groupRepository = RemoteGroupDataSource()
    private lateinit var groupName: String
    private lateinit var messageRepository: MessageRepository
    private val viewModel: SocketViewModel by viewModels { SocketViewModelFactory(groupRepository, groupName, messageRepository) }
    private val groupViewModel: GroupViewModel by viewModels()

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySocketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from the intent
        groupId = intent.getStringExtra("idGroup")?.toInt()
        groupName = intent.getStringExtra("groupName") ?: ""

        binding.toolbar.title = groupName

        messageAdapter = MessageAdapter()
        binding.listMessages.adapter = messageAdapter

        if (groupId != null) {
            viewModel.thisGroupsMessages(groupId!!)
        }

        viewModel.messages.observe(this) { result ->
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    result.data?.let { data ->
                        messageAdapter.submitList(data)
                    }
                }
                Resource.Status.ERROR -> {
                    // Handle error
                    Toast.makeText(
                        this,
                        "Error fetching information, please restart",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Resource.Status.LOADING -> {
                    // Handle loading state (optional)
                }
            }
        }
        binding.btnSendMessage.isEnabled = false

        onConnectedChange(binding)
        onMessagesChange()
        buttonsListeners(binding)

        Log.d("ButtonClickListener", "Connect button clicked")
        viewModel.startSocket()
    }


    private fun onConnectedChange(binding: ActivitySocketBinding) {
        viewModel.connected.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    binding.btnSendMessage.isEnabled = it.data!!
                }
                Resource.Status.ERROR -> {
                    // TODO sin gestionarlo en el VM. Y si envia en una sala que ya no esta? a tratar
                    Log.d(TAG, "error al conectar...")
                    binding.btnSendMessage.isEnabled = it.data!!
                }
                Resource.Status.LOADING -> {
                    binding.btnSendMessage.isEnabled = it.data!!
                }
            }
        })
    }
    private fun onMessagesChange() {
        viewModel.messages.observe(this, Observer {
            Log.d(TAG, "messages change")
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    Log.d(TAG, "messages observe success")
                    if (!it.data.isNullOrEmpty()) {
                        messageAdapter.submitList(it.data)
                        messageAdapter.notifyDataSetChanged()
                    }
                }
                Resource.Status.ERROR -> {
                    Log.d(TAG, "messages observe error")
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
                Resource.Status.LOADING -> {
                    // de momento
                    Log.d(TAG, "messages observe loading")
                    val toast = Toast.makeText(this, "Cargando..", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun buttonsListeners(binding: ActivitySocketBinding) {
        binding.btnSendMessage.setOnClickListener {
            Log.d("ButtonClickListener", "Send Message button clicked")
            val message = binding.inputMessage.text.toString()
            binding.inputMessage.setText("")
            viewModel.onSendMessage(message)
        }
        binding.leaveButton.setOnClickListener {
            Log.d("ButtonClickListener", "Leave Chat button clicked")

            lifecycleScope.launch {
                try {
                    val userId = appDatabase.getUserDao().getAllUser().first().id
                    groupId?.let { it1 -> groupViewModel.leaveChat(userId!!, it1) }
                } catch (e: Exception) {
                    // Handle exceptions if any
                    Log.e("ButtonClickListener", "Error getting user ID: ${e.message}")
                }
            }
        }

        groupViewModel.leaveChatResult.observe(this, Observer { result ->
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    Toast.makeText(this, "Successfully left the chat", Toast.LENGTH_SHORT).show()
                    // You can perform additional actions on success if needed
                    //val group = obtainGroupById(groupId)

                    // Verificar si el objeto Group se obtuvo correctamente y cambiar isUserOnGroup a false
                    //group?.let {
                    //    it.isUserOnGroup = false
                    //    // Actualizar la lista de grupos
                    //    groupViewModel.updateGroupList()
                    //}

                    // Set the result to indicate success
                    setResult(Activity.RESULT_OK)
                    // Finish the current activity

                }
                Resource.Status.ERROR -> {
                    Toast.makeText(this, "Error leaving the chat: ${result.message}", Toast.LENGTH_SHORT).show()
                    // Handle error state, if needed
                }
                Resource.Status.LOADING -> {
                    // You can handle loading state if needed
                }
            }
        })
    }
}
