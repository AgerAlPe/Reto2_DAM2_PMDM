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
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class SocketViewModel @Inject constructor(
    private val groupRepository: CommonGroupRepository,
    private val messageDao: MessageDao
) : ViewModel() {

    private val TAG = "SocketViewModel"

    private val _messages = MutableLiveData<Resource<List<SocketMessage>>>()
    val messages : LiveData<Resource<List<SocketMessage>>> get() = _messages

    private val _connected = MutableLiveData<Resource<Boolean>>()
    val connected : LiveData<Resource<Boolean>> get() = _connected

    private val _testMessages = MutableLiveData<Resource<List<Message>>>()
    val testMessages : LiveData<Resource<List<Message>>> get() = _testMessages

    private val SOCKET_HOST = "http://10.5.7.39:8085/"
    private val AUTHORIZATION_HEADER = "Authorization"

    private lateinit var mSocket: Socket

    // TODO esto esta hardcodeeado
    private val SOCKET_ROOM = "default-room"

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

    private fun onNewMessageJsonObject(data : Any) {
        try {
            val jsonObject = data as JSONObject
            val jsonObjectString = jsonObject.toString()
            val message = Gson().fromJson(jsonObjectString, SocketMessageRes::class.java)

            Log.d(TAG, message.authorName)
            Log.d(TAG, message.messageType.toString())

            updateMessageListWithNewMessage(message)
        } catch (ex: Exception) {
            Log.e(TAG, ex.message!!)
        }
    }

    private fun updateMessageListWithNewMessage(socketMessage: SocketMessageRes) {
        try {
            val incomingMessage = SocketMessage(SOCKET_ROOM, socketMessage.message, socketMessage.authorName)
            val msgsList = _messages.value?.data?.toMutableList()
            if (msgsList != null) {
                msgsList.add(incomingMessage)
                //TODO Hay que mirar esto para guarda los mensajes que llegan
                /*viewModelScope.launch {
                    val messageEntity = MessageEntity(
                        text = incomingMessage.text,
                        authorId = incomingMessage.authorId,
                        chatId = incomingMessage.chatId
                    )
                    messageDao.insertMessage(messageEntity)
                }*/
                _messages.postValue(Resource.success(msgsList))
            } else {
                _messages.postValue(Resource.success(listOf(incomingMessage)))
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message!!)
        }
    }

    fun onSendMessage(message: String) {
        Log.d(TAG, "onSendMessage $message")
        // la sala esta hardcodeada..
        val socketMessage = SocketMessageReq(SOCKET_ROOM, message)
        val jsonObject = JSONObject(Gson().toJson(socketMessage))
        mSocket.emit(SocketEvents.ON_SEND_MESSAGE.value, jsonObject)
    }
}


class SocketViewModelFactory(
    private val groupRepository: CommonGroupRepository,
    private val messageDao: MessageDao
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return SocketViewModel(groupRepository, messageDao) as T
    }
}