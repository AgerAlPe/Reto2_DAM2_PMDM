package com.grupo2.elorchat.ui.socket

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.grupo2.elorchat.R
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.repository.MessageRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.data.repository.remote.RemoteSocketDataSource
import com.grupo2.elorchat.databinding.ActivitySocketBinding
import com.grupo2.elorchat.ui.groups.GroupViewModel
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.inject.Inject
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.core.app.ActivityCompat


@AndroidEntryPoint
class SocketActivity() : ComponentActivity() {

    private val TAG = "SocketActivity"
    private var groupId: Int? = null
    private lateinit var messageAdapter: MessageAdapter
    private val groupRepository = RemoteGroupDataSource()
    private val socketRepository = RemoteSocketDataSource()
    private lateinit var groupName: String
    @Inject
    lateinit var messageRepository: MessageRepository
    private val viewModel: SocketViewModel by viewModels { SocketViewModelFactory(groupRepository, messageRepository, socketRepository, groupName) }
    private val groupViewModel: GroupViewModel by viewModels()
    private val socketViewModelt: SocketViewModel by viewModels()

    private lateinit var socketService: SocketService
    private var isBind = false

    private val PICK_IMAGE_REQUEST = 1
    private var base64Image: String? = null

    private val PICK_FILE_REQUEST = 2

    private lateinit var locationManager: LocationManager



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

        val addMessageImageView: ImageView = findViewById(R.id.addMessageImageView)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager



        addMessageImageView.setOnClickListener { view ->
            showPopupMenu(view)
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
        // binding.btnSendMessage.isEnabled = false

        onConnectedChange(binding)
        onMessagesChange()
        buttonsListeners(binding)

        Log.d("ButtonClickListener", "Connect button clicked")
    }

    fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.message_type, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.send_photo -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                    return@setOnMenuItemClickListener true
                }
                R.id.send_coordinates -> {
                    sendCoordinates { coordinates ->
                        // Send the coordinates as the message content
                        socketService.sendMessage(coordinates, groupName)
                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.send_file -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "*/*"
                    startActivityForResult(intent, PICK_FILE_REQUEST)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }

        popupMenu.show()
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
        val messageContent = binding.inputMessage.text.toString()
        binding.inputMessage.setText("")
        // Store base64Image in a local variable
        val localBase64Image = base64Image
        // Reset the base64Image after storing it
        base64Image = null

        // Check if an image is selected
        if (localBase64Image != null) {
            // Send the message with the image data
            socketService.sendMessage(localBase64Image, groupName)
        } else {
            // Send the message without the image data
            socketService.sendMessage(messageContent, groupName)
        }
    }

        binding.leaveButton.setOnClickListener {
            Log.d("ButtonClickListener", "Leave Chat button clicked")

            lifecycleScope.launch {
                try {
                    val userId = appDatabase.getUserDao().getAllUser().first().id

                    groupId?.let { it1 -> groupViewModel.leaveChat(userId!!, it1) }

                    socketViewModelt.leaveRoom(groupName, userId!!)
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
                    groupViewModel.updateGroupList()
                    // Set the result to indicate success
                    setResult(Activity.RESULT_OK)
                    finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    // Handle image selection
                    val imageUri = data?.data
                    val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        val base64Image = bitmapToBase64(bitmap)
                        // Send the message with the image data
                        socketService.sendMessage(base64Image, groupName)
                    } else {
                        // Handle the case where bitmap is null
                        Toast.makeText(this, "Failed to decode image data", Toast.LENGTH_SHORT).show()
                    }
                }
                PICK_FILE_REQUEST -> {
                    // Handle file selection
                    val fileUri = data?.data
                    val inputStream = fileUri?.let { contentResolver.openInputStream(it) }
                    val byteArray = inputStream?.readBytes()
                    inputStream?.close()

                    if (byteArray != null) {
                        val base64File = Base64.getEncoder().encodeToString(byteArray)
                        // Send the message with the file data
                        socketService.sendMessage(base64File, groupName)
                    } else {
                        // Handle the case where byteArray is null
                        Toast.makeText(this, "Failed to read file data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 20, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    @SuppressLint("MissingPermission")
    private fun sendCoordinates(callback: (String) -> Unit) {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object :
                LocationListener {
                override fun onLocationChanged(location: Location) {
                    // Send the coordinates
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val coordinatesMessage = "Latitude: $latitude, Longitude: $longitude"
                    // Pass the coordinates message to the callback function
                    callback.invoke(coordinatesMessage)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, null)
        } else {
            // Permission not granted, request it from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1001
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, SocketService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (isBind) {
            unbindService(serviceConnection)
            EventBus.getDefault().unregister(this)
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationMessage(message: Message) {
        // Notify the ViewModel about the new message
        viewModel.onNewMessageReceived(message)
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val localService = service as SocketService.SocketBinder
            socketService = localService.getService()
            isBind = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBind = false
        }
    }

}
