package com.grupo2.elorchat.ui.socket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.database.repository.MessageRepository
import com.grupo2.elorchat.data.socket.SocketEvents
import com.grupo2.elorchat.data.socket.SocketMessageReq
import com.grupo2.elorchat.utils.Resource
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
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class SocketService : Service() {
    private lateinit var serviceScope: CoroutineScope
    private val TAG = "SocketService"
    private lateinit var mSocket: Socket
    private val SOCKET_HOST = "http://10.5.7.39:8085/"
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
        serviceScope = CoroutineScope(Dispatchers.Default)
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

    fun connect() {
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

    // esto es cuando recibo el mensaje
    private fun onNewMessage(): Emitter.Listener {
        return Emitter.Listener {
            // en teoria deberia ser siempre jsonObject, obviamente si siempre lo gestionamos asi
            if (it[0] is JSONObject) {
                onNewMessageJsonObject(it[0])
            }
        }
    }

    private fun onNewMessageJsonObject(data: Any) {
        Log.i(TAG, "Mensaje que envias: " + data.toString())
        try {
            val jsonObject = data as JSONObject
            val jsonObjectString = jsonObject.toString()

            // Assuming you have a MessageResponse class for deserialization
            val message = Gson().fromJson(jsonObjectString, Message::class.java)
            message.createdAt = LocalDateTime.now().toString()
            // Creating a Message instance from MessageResponse
            Log.i(TAG, "Fecha: " + message.createdAt)

            updateMessageListWithNewMessage(message)
        } catch (ex: Exception) {
            Log.e(TAG, "onNewMessageJsonObject " + ex.message ?: "Unknown error")
        }
    }

    private fun updateMessageListWithNewMessage(incomingMessage: Message) {
        try {
            Log.i(TAG, incomingMessage.toString())
                serviceScope.launch {
                    withContext(Dispatchers.IO) {
                        messageRepository.insertMessage(incomingMessage)
                        Log.i(TAG, "Añadido a Room: " + incomingMessage.toString())
                    }
                    EventBus.getDefault().post(incomingMessage)
                }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message ?: "Unknown error")
        }
    }

    // yo soy quien envia este mensaje
    fun sendMessage(message: String, groupName: String) {
        // Send message through the socket
        // val jsonObject = createMessageJsonObject(message)
        Log.d(TAG, "sendMessage a enviar $message $groupName")
        val socketMessage = SocketMessageReq(groupName, message)
        val jsonObject = JSONObject(Gson().toJson(socketMessage))
        mSocket.emit(SocketEvents.ON_SEND_MESSAGE.value, jsonObject)
        Log.d(TAG, "sendMessage enviado")
    }

    private fun onJoinRoom(): Emitter.Listener {
        return Emitter.Listener {
            if (it[0] is String) {
                onJoinRoomString(it[0] as String)
            }
        }
    }

    private fun onJoinRoomString(data: String) {
        // Handle the data received when a user joins a room
        Log.i(TAG, "User joined a room: $data")
        // You can process the data or perform any action as needed
    }

    private fun onLeftRoom(): Emitter.Listener {
        return Emitter.Listener {
            if (it[0] is String) {
                onLeftRoomString(it[0] as String)
            }
        }
    }

    private fun onLeftRoomString(data: String) {
        // Handle the data received when a user joins a room
        Log.i(TAG, "User left a room: $data")
        // You can process the data or perform any action as needed
    }
/*
    private fun createMessageJsonObject(message: String): JSONObject {
        // Create and return a JSON object for the message
        // Customize this method based on your message format
        return JSONObject().put("message", message)
    }
*/
    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
    }

    private fun disconnectSocket() {
        Log.i(TAG, "Disconnecting socket")
        mSocket.disconnect()
        Log.i(TAG, "Socket disconnected")
    }
}
