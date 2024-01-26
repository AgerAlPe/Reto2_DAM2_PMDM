package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.utils.Resource

interface CommonMessageRepository {

    suspend fun getMessages() : Resource<List<MessageEntity>>

}