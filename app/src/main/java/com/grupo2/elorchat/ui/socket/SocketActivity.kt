package com.grupo2.elorchat.ui.socket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.grupo2.elorchat.R
import com.grupo2.elorchat.databinding.ActivitySocketBinding
import com.grupo2.elorchat.utils.Resource

class SocketActivity : ComponentActivity() {

    private val TAG = "SocketActivity"
    private lateinit var messageAdapter: MessageAdapter
    private val viewModel: SocketViewModel by viewModels { SocketViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySocketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageAdapter = MessageAdapter()
        binding.listMessages.adapter = messageAdapter

        binding.btnSendMessage.isEnabled = false

        onConnectedChange(binding)
        onMessagesChange()
        buttonsListeners(binding)
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
        binding.btnConnect.setOnClickListener {
            Log.d("ButtonClickListener", "Connect button clicked")
            viewModel.startSocket()
        }

        binding.btnSendMessage.setOnClickListener {
            Log.d("ButtonClickListener", "Send Message button clicked")
            val message = binding.inputMessage.text.toString()
            binding.inputMessage.setText("")
            viewModel.onSendMessage(message)
        }
    }
}