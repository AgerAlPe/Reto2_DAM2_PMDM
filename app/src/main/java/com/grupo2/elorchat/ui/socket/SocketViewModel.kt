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
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.socket.SocketEvents
import com.grupo2.elorchat.data.socket.SocketMessageReq
import com.grupo2.elorchat.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime.now
import javax.inject.Inject

@HiltViewModel
class SocketViewModel @Inject constructor(
    private val groupRepository: CommonGroupRepository,
    private val groupName: String?
) : ViewModel() {

    private val TAG = "SocketViewModel"

    private val _messages = MutableLiveData<Resource<List<Message>>>()
    val messages : LiveData<Resource<List<Message>>> get() = _messages

    private val _connected = MutableLiveData<Resource<Boolean>>()
    val connected : LiveData<Resource<Boolean>> get() = _connected

    private val SOCKET_ROOM = groupName

    fun thisGroupsMessages(groupId : Int) {
        Log.i(TAG, "Id Grupo: " + groupId)
        viewModelScope.launch {
            _messages.value =  showThisGroupsMessages(groupId)
            Log.i(TAG, "Lista mensajes: " + _messages.value)
        }
    }



    private suspend fun showThisGroupsMessages(groupId: Int): Resource<List<Message>> {
        return withContext(Dispatchers.IO) {
            groupRepository.getMessagesFromGroup(groupId)
        }
    }


    fun onSendMessage(message: String) {
        Log.d(TAG, "onSendMessage $message")
        // la sala esta hardcodeada..
        val socketMessage = SOCKET_ROOM?.let { SocketMessageReq(it, message) }
        val jsonObject = JSONObject(Gson().toJson(socketMessage))
        // mSocket.emit(SocketEvents.ON_SEND_MESSAGE.value, jsonObject)

    }

    fun onNewMessageReceived(message: Message) {
        viewModelScope.launch {
            // Update the LiveData with the new message
            val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
            currentMessages.add(message)
            _messages.value = Resource.success(currentMessages)
        }
    }
}


class SocketViewModelFactory(
    private val groupRepository: CommonGroupRepository,
    private val groupName: String?
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return SocketViewModel(groupRepository, groupName) as T
    }
}