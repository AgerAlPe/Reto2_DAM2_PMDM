package com.grupo2.elorchat.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.repository.CommonMessageRepository
import com.grupo2.elorchat.data.repository.remote.RemoteMessageDataSource
import com.grupo2.elorchat.utils.Resource
import kotlinx.coroutines.launch

class MessageViewModel(private val commonMessageRepository: CommonMessageRepository) : ViewModel() {

    private val _allMessages = MutableLiveData<Resource<List<MessageEntity>>>()
    val allMessages: LiveData<Resource<List<MessageEntity>>> get() = _allMessages

    // Empty constructor for ViewModelProvider
    constructor() : this(RemoteMessageDataSource()) {
        // You can provide a default repository or leave it empty
    }

    fun fetchAllMessagesFromRoom() {
        viewModelScope.launch {
            try {
                _allMessages.value = Resource.loading(null)

                // Fetch messages from your repository
                val messages = commonMessageRepository.getMessages()

                // Directly assign the resource to _allMessages.value
                _allMessages.value = messages
            } catch (e: Exception) {
                _allMessages.value = Resource.error("Error fetching messages: ${e.message}", null)
            }
        }
    }
}