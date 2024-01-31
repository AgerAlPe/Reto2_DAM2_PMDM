package com.grupo2.elorchat.data.database.repository

import com.grupo2.elorchat.data.database.dao.ChatUserDao
import com.grupo2.elorchat.data.database.entities.ChatUserEntity
import com.grupo2.elorchat.utils.Resource
import javax.inject.Inject

class ChatUserRepository @Inject constructor(private val chatUserDao: ChatUserDao) {

    suspend fun insertChatUser(chatUser: ChatUserEntity) {
        chatUserDao.insertChatUser(chatUser)
    }

    suspend fun getChatUser(userId: Int, chatId: Int): ChatUserEntity? {
        return chatUserDao.getChatUser(userId, chatId)
    }

    suspend fun deleteChatUser(userId: Int, chatId: Int) {
        chatUserDao.deleteChatUser(userId, chatId)
    }

    suspend fun getChatUsersInChat(chatId: Int): Resource<List<ChatUserEntity>> {
        return Resource.success(chatUserDao.getChatUsersInChat(chatId))
    }
}



