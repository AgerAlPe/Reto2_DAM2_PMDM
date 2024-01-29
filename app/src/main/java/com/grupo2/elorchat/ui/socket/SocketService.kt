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
import com.grupo2.elorchat.data.socket.SocketEvents
import com.grupo2.elorchat.data.socket.SocketMessageReq
import com.grupo2.elorchat.utils.Resource
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

class SocketService : Service() {
    private lateinit var serviceScope: CoroutineScope
    private val TAG = "SocketService"
    private lateinit var mSocket: Socket
    private val SOCKET_HOST = "http://10.5.7.39:8085/"
    private val AUTHORIZATION_HEADER = "Authorization"

    private val mBinder: IBinder = SocketBinder()

    private lateinit var messageDao : MessageDao


    inner class SocketBinder : Binder() {
        fun getService(): SocketService = this@SocketService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.Default)
        messageDao = AppDatabase.getInstance(application).getMessageDao()
        startSocket()
    }

    private fun startSocket() {
        Log.i(TAG, "Starting socket")

        val socketOptions = createSocketOptions()
        mSocket = IO.socket(SOCKET_HOST, socketOptions)

        mSocket.on(SocketEvents.ON_CONNECT.value, onConnect())
        mSocket.on(SocketEvents.ON_DISCONNECT.value, onDisconnect())
        mSocket.on(SocketEvents.ON_MESSAGE_RECEIVED.value, onNewMessage())

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

    private fun updateMessageListWithNewMessage(incommingMessage: Message) {
        try {
            val messageEntity = MessageEntity(
                text = incommingMessage.message,
                authorId = incommingMessage.userId,
                chatId =  incommingMessage.chatId
            )

            serviceScope.launch {
                withContext(Dispatchers.IO) {
                    messageDao.insertMessage(messageEntity)
                }
                EventBus.getDefault().post(messageEntity)

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
