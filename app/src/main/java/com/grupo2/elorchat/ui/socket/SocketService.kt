package com.grupo2.elorchat.ui.socket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.google.gson.Gson
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.database.repository.MessageRepository
import com.grupo2.elorchat.data.socket.SocketEvents
import com.grupo2.elorchat.data.socket.SocketMessageReq
import com.grupo2.elorchat.ui.groups.GroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.lifecycle.ViewModelStoreOwner


@AndroidEntryPoint
class SocketService() : Service(), ViewModelStoreOwner {


    private val viewModel: SocketViewModel by lazy {
        ViewModelProvider(this).get(SocketViewModel::class.java)
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val TAG = "SocketService"
    private lateinit var mSocket: Socket
    private val SOCKET_HOST = "http://10.5.7.89:8085/"
    private val AUTHORIZATION_HEADER = "Authorization"

    private val mBinder: IBinder = SocketBinder()

    @Inject
    lateinit var messageRepository: MessageRepository

    inner class SocketBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        startSocket()
    }

    private fun startSocket() {
        Log.i(TAG, "Starting socket")

        val socketOptions = createSocketOptions()
        mSocket = IO.socket(SOCKET_HOST, socketOptions)

        mSocket.on(SocketEvents.ON_CONNECT.value, onConnect())
        mSocket.on(SocketEvents.ON_DISCONNECT.value, onDisconnect())
        mSocket.on(SocketEvents.ON_MESSAGE_RECEIVED.value, onNewMessage())
        mSocket.on(SocketEvents.ON_JOINED_ROOM.value, onJoinRoom())
        mSocket.on(SocketEvents.ON_LEFT_ROOM.value, onLeftRoom())
        connect()
    }

    private fun connect() {
        Log.i(TAG, "Connecting to the socket")
        mSocket.connect()
        Log.i(TAG, "Socket connected")
    }

    private fun createSocketOptions(): IO.Options {
        val options = IO.Options()

        // Add custom headers
        val headers = mutableMapOf<String, MutableList<String>>()
        headers[AUTHORIZATION_HEADER] = mutableListOf(ElorChat.userPreferences.fetchAuthToken().toString())
        options.extraHeaders = headers

        return options
    }

    private fun onConnect(): Emitter.Listener {
        return Emitter.Listener {
            Log.d(TAG, "Connected to the socket")
            // Handle the connection event
        }
    }

    private fun onDisconnect(): Emitter.Listener {
        return Emitter.Listener {
            Log.d(TAG, "Disconnected from the socket")
            // Handle the disconnection event
        }
    }

    private fun onNewMessage(): Emitter.Listener {
        return Emitter.Listener {
            if (it[0] is JSONObject) {
                onNewMessageJsonObject(it[0])
            }
        }
    }

    private fun onNewMessageJsonObject(data: Any) {
        Log.i(TAG, "Message received: $data")
        try {
            val jsonObject = data as JSONObject
            val jsonObjectString = jsonObject.toString()

            val message = Gson().fromJson(jsonObjectString, Message::class.java)
            message.createdAt = LocalDateTime.now().toString()
            Log.i(TAG, "Date: ${message.createdAt}")

            updateMessageListWithNewMessage(message)
        } catch (ex: Exception) {
            Log.e(TAG, "Error processing new message: ${ex.message ?: "Unknown error"}")
        }
    }

    private fun updateMessageListWithNewMessage(incomingMessage: Message) {
        try {
            Log.i(TAG, "Incoming message: $incomingMessage")
            serviceScope.launch {
                withContext(Dispatchers.IO) {
                    messageRepository.insertMessage(incomingMessage)
                    Log.i(TAG, "Added to Room: $incomingMessage")
                }
                EventBus.getDefault().post(incomingMessage)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating message list: ${ex.message ?: "Unknown error"}")
        }
    }

    fun sendMessage(message: String, groupName: String, userId: Int, chatId: Int) {
        Log.d(TAG, "Sending message: $message to group: $groupName")
        val socketMessage = SocketMessageReq(groupName, message)
        val jsonObject = JSONObject(Gson().toJson(socketMessage))
        serviceScope.launch {
            try {
                val messageEntity = MessageEntity(
                    1,
                    message = message,
                    userId = userId,
                    chatId = chatId,
                    createdAt = getCurrentDateTime(),
                    1
                )
                messageRepository.insertMessageEntity(messageEntity)
                viewModel.fetchRoomMessages(chatId)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message: ${e.message}")
            }
        }
        mSocket.emit(SocketEvents.ON_SEND_MESSAGE.value, jsonObject)
        Log.d(TAG, "Message sent")
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun onJoinRoom(): Emitter.Listener {
        return Emitter.Listener {
            if (it[0] is String) {
                onJoinRoomString(it[0] as String)
            }
        }
    }

    private fun onJoinRoomString(data: String) {
        Log.i(TAG, "User joined a room: $data")
    }

    private fun onLeftRoom(): Emitter.Listener {
        return Emitter.Listener {
            if (it[0] is String) {
                onLeftRoomString(it[0] as String)
            }
        }
    }

    private fun onLeftRoomString(data: String) {
        Log.i(TAG, "User left a room: $data")
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
    }

    private fun disconnectSocket() {
        Log.i(TAG, "Disconnecting socket")
        mSocket.disconnect()
        Log.i(TAG, "Socket disconnected")
    }

    override val viewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }
}
