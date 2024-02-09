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
import com.grupo2.elorchat.data.database.entities.toMessage
import com.grupo2.elorchat.data.database.entities.toMessageEntity
import com.grupo2.elorchat.data.database.repository.MessageRepository
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.CommonSocketRepository
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime.now
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SocketViewModel @Inject constructor(
    private val groupRepository: CommonGroupRepository,

    private val groupName: String?,
    private val messageRepository: MessageRepository,
    private val socketRepository: CommonSocketRepository,
) : ViewModel() {

    private val TAG = "SocketViewModel"

    private val _messages = MutableLiveData<Resource<List<Message>>>()
    val messages : LiveData<Resource<List<Message>>> get() = _messages

    private val _connected = MutableLiveData<Resource<Boolean>>()
    val connected : LiveData<Resource<Boolean>> get() = _connected

    val joined : LiveData<Resource<Void>> get() = _joined

    private val _joined = MutableLiveData<Resource<Void>>()


    val leave : LiveData<Resource<Void>> get() = _leave

    private val _leave = MutableLiveData<Resource<Void>>()

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

    fun onNewMessageReceived(message: Message) {
        viewModelScope.launch {
            // Update the LiveData with the new message
            val currentMessages = _messages.value?.data?.toMutableList() ?: mutableListOf()
            currentMessages.add(message)
            _messages.value = Resource.success(currentMessages)
        }
    }

    fun joinRoom(room : String, isAdmin : Boolean) {
        viewModelScope.launch {
            _joined.value = joinSocketRoom(room , isAdmin)
        }
    }

    fun leaveRoom(room : String) {
        viewModelScope.launch {
            _leave.value = leaveSocketRoom(room)
        }
    }

    private suspend fun joinSocketRoom(room : String, isAdmin : Boolean): Resource<Void> {

        return withContext(Dispatchers.IO) {
            socketRepository.joinRoom(room , isAdmin)
        }
    }

    private suspend fun leaveSocketRoom(room : String): Resource<Void> {
        return withContext(Dispatchers.IO) {
            socketRepository.leaveRoom(room)
        }
    }


}

class SocketViewModelFactory(
    private val groupRepository: CommonGroupRepository,
    private val messageRepository: MessageRepository,
    private val socketRepository: CommonSocketRepository,
    private val groupName: String?
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return SocketViewModel(groupRepository, groupName, messageRepository, socketRepository) as T
    }
}