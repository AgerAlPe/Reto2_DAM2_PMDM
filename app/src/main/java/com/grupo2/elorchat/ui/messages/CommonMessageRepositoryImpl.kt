package com.grupo2.elorchat.ui.messages

import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.repository.CommonMessageRepository
import com.grupo2.elorchat.data.repository.remote.RemoteMessageDataSource
import com.grupo2.elorchat.utils.Resource

class CommonMessageRepositoryImpl(
    private val remoteDataSource: RemoteMessageDataSource,
    private val messageDao: MessageDao
) : CommonMessageRepository {

    override suspend fun getMessages(): Resource<List<MessageEntity>> {
        return try {
            // Fetch messages from the remote data source (API)
            val remoteResponse = remoteDataSource.getMessages()

            if (remoteResponse.status == Resource.Status.SUCCESS) {
                // Save messages to the local database (Room)
                remoteResponse.data?.let { messages ->
                    val messageEntities = messages.map { it.toEntity() }
                    messageDao.insertAll(messageEntities)
                }
            }

            remoteResponse // Return the original response
        } catch (e: Exception) {
            // Handle exceptions if needed
            Resource.error("Error fetching messages: ${e.message}", null)
        }
    }
}

