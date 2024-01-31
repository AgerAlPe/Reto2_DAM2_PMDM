package com.grupo2.elorchat.data.database.repository

import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.entities.toMessage
import com.grupo2.elorchat.data.database.entities.toMessageEntity
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val userRepository: UserRepository
) {

    suspend fun getAllMessages(): List<Message> {
        val messageEntities = messageDao.getAllMessage()
        return messageEntities.map { it.toMessage(userRepository) }
    }

    suspend fun getAllUserMessages(userId: Int): List<Message> {
        val messageEntities = messageDao.getAllUserMessage(userId)
        return messageEntities.map { it.toMessage(userRepository) }
    }

    suspend fun insertMessages(messages: List<Message>) {
        val messageEntities = messages.map { it.toMessageEntity() }
        messageDao.insertAll(messageEntities)
    }

    suspend fun insertMessage(message: Message) {
        val messageEntity = message.toMessageEntity()
        messageDao.insertMessage(messageEntity)
    }
}
