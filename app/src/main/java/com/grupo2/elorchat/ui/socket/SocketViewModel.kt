package com.grupo2.elorchat.ui.socket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.gson.Gson
import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.SocketMessage
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.socket.SocketEvents
import com.grupo2.elorchat.data.socket.SocketMessageReq
import com.grupo2.elorchat.data.socket.SocketMessageRes
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.Timestamp
import javax.inject.Inject

@HiltViewModel
class SocketViewModel @Inject constructor(
    private val groupRepository: CommonGroupRepository,
    private val groupName: String?,
    private val messageDao: MessageDao
) : ViewModel() {

    private val TAG = "SocketViewModel"

    private val _messages = MutableLiveData<Resource<List<Message>>>()
    val messages : LiveData<Resource<List<Message>>> get() = _messages

    private val _connected = MutableLiveData<Resource<Boolean>>()
    val connected : LiveData<Resource<Boolean>> get() = _connected

    private val _testMessages = MutableLiveData<Resource<List<Message>>>()
    val testMessages : LiveData<Resource<List<Message>>> get() = _testMessages

    private val SOCKET_HOST = "http://10.5.7.39:8085/"
    private val AUTHORIZATION_HEADER = "Authorization"

    private lateinit var mSocket: Socket

    private val SOCKET_ROOM = groupName

    fun startSocket() {
        Log.i("SocketViewModel", "Starting socket")

        val socketOptions = createSocketOptions()
        mSocket = IO.socket(SOCKET_HOST, socketOptions)

        mSocket.on(SocketEvents.ON_CONNECT.value, onConnect())
        mSocket.on(SocketEvents.ON_DISCONNECT.value, onDisconnect())
        mSocket.on(SocketEvents.ON_MESSAGE_RECEIVED.value, onNewMessage())

        viewModelScope.launch {
            Log.i("SocketViewModel", "Connecting to the socket")
            connect()
            Log.i("SocketViewModel", "connect fun3")
        }
    }

    fun thisGroupsMessages(groupId : Int) {
        viewModelScope.launch {
            _testMessages.value =  showThisGroupsMessages(groupId)
        }
    }

    private suspend fun showThisGroupsMessages(groupId: Int): Resource<List<Message>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getMessagesFromGroup(groupId)
        }
    }

    private suspend fun connect() {
        withContext(Dispatchers.IO) {
            Log.i("SocketViewModel", "connect fun")
            mSocket.connect();
            Log.i("SocketViewModel", "connect fun2")
        }
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
            // Manejar el mensaje recibido
            Log.d(TAG, "conectado")

            // no vale poner value por que da error al estar en otro hilo
            // IllegalStateException: Cannot invoke setValue on a background thread
            // en funcion asincrona obligado post
            _connected.postValue(Resource.success(true))
        }
    }
    private fun onDisconnect(): Emitter.Listener {
        return Emitter.Listener {
            Log.d(TAG, "desconectado")
            _connected.postValue(Resource.success(false))
        }
    }

    private fun onNewMessage(): Emitter.Listener {
        return Emitter.Listener {
            // en teoria deberia ser siempre jsonObject, obviamente si siempre lo gestionamos asi
            if (it[0] is JSONObject) {
                onNewMessageJsonObject(it[0])
            } else if (it[0] is String) {
                onNewMessageString(it[0])
            }
        }
    }

    private fun onNewMessageString(data: Any) {
        try {
            // Manejar el mensaje recibido
            val message = data as String
            Log.d(TAG, "mensaje recibido $message")
            // ojo aqui no estoy actualizando la lista. aunque no deberiamos recibir strings
        } catch (ex: Exception) {
            Log.e(TAG, ex.message!!)
        }
    }

    private fun onNewMessageJsonObject(data: Any) {
        try {
            val jsonObject = data as JSONObject
            val jsonObjectString = jsonObject.toString()

            // Assuming you have a MessageResponse class for deserialization
            val messageResponse = Gson().fromJson(jsonObjectString, Message::class.java)

            // Creating a Message instance from MessageResponse
            val message = Message(
                id = messageResponse.id,
                message = messageResponse.message,
                name = messageResponse.name,
                userId = messageResponse.userId,
                chatId = messageResponse.chatId,
                createdAt = messageResponse.createdAt // Assuming messageResponse.createdAt is already a Timestamp
            )

            Log.d("mensajeSocket", "ID: ${message.id}")
            Log.d("mensajeSocket", "Message: ${message.message}")
            Log.d("mensajeSocket", "Name: ${message.name}")
            Log.d("mensajeSocket", "UserId: ${message.userId}")
            Log.d("mensajeSocket", "GroupId: ${message.chatId}")
            Log.d("mensajeSocket", "CreatedAt: ${message.createdAt}")
            
            updateMessageListWithNewMessage(message)
        } catch (ex: Exception) {
            Log.e(TAG, ex.message ?: "Unknown error")
        }
    }

//    private fun updateMessageListWithNewMessage(socketMessage: SocketMessageRes) {
//        try {
//            val incomingMessage =
//                SOCKET_ROOM?.let { SocketMessage(it, socketMessage.message, socketMessage.authorName) }
//            val msgsList = _messages.value?.data?.toMutableList()
//            if (msgsList != null) {
//                if (incomingMessage != null) {
//                    msgsList.add(incomingMessage)
//                }
//                _messages.postValue(Resource.success(msgsList))
//            } else {
//                _messages.postValue(Resource.success(listOf(incomingMessage)) as Resource<List<SocketMessage>>?)
//            }
//        } catch (ex: Exception) {
//            Log.e(TAG, ex.message!!)
//        }
//    }

    private fun updateMessageListWithNewMessage(socketMessage: Message) {
        try {
            val incomingMessage = SOCKET_ROOM?.let {
                Message(
                    id = socketMessage.id,  // You might need to set an appropriate default value for id
                    message = socketMessage.message,
                    name = socketMessage.name,
                    userId = socketMessage.userId,  // You might need to set an appropriate default value for userId
                    chatId = socketMessage.chatId,  // You might need to set an appropriate default value for groupId
                    createdAt = socketMessage.createdAt  // You might need to set an appropriate default value for createdAt
                )
            }

            val messageEntity = MessageEntity(
                text = socketMessage.message,
                authorId = socketMessage.userId,
                chatId =  socketMessage.chatId
            )

            viewModelScope.launch {
                messageDao.insertMessage(messageEntity)
            }

            val msgsList = _messages.value?.data?.toMutableList()
            Log.i(TAG, "Estado de la lista:" + msgsList.toString())
            if (msgsList != null) {

                if (!incomingMessage.toString().isNullOrEmpty()) {
                    incomingMessage?.let { msgsList.add(it) }
                }

                _messages.postValue(Resource.success(msgsList))
            } else {
                _messages.postValue(Resource.success(listOfNotNull(incomingMessage)))
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message ?: "Unknown error")
        }
    }

    fun onSendMessage(message: String) {
        Log.d(TAG, "onSendMessage $message")
        // la sala esta hardcodeada..
        val socketMessage = SOCKET_ROOM?.let { SocketMessageReq(it, message) }
        val jsonObject = JSONObject(Gson().toJson(socketMessage))
        mSocket.emit(SocketEvents.ON_SEND_MESSAGE.value, jsonObject)
    }
}


class SocketViewModelFactory(
    private val groupRepository: CommonGroupRepository,
    private val groupName: String?,
    private val messageDao: MessageDao
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return SocketViewModel(groupRepository, groupName, messageDao) as T
    }
}