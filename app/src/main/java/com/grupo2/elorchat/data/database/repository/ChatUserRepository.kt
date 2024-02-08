package com.grupo2.elorchat.data.database.repository

import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.data.database.dao.ChatUserDao
import com.grupo2.elorchat.data.database.entities.ChatUserEntity
import com.grupo2.elorchat.data.database.entities.toChatUser
import com.grupo2.elorchat.data.database.entities.toChatUserEntity
import com.grupo2.elorchat.utils.Resource
import javax.inject.Inject

class ChatUserRepository @Inject constructor(private val chatUserDao: ChatUserDao) {

    suspend fun insertChatUser(chatUser: ChatUser) {
        chatUserDao.insertChatUser(chatUser.toChatUserEntity())
    }

    suspend fun getChatUser(userId: Int, chatId: Int): ChatUser? {
        val chatUserEntity = chatUserDao.getChatUser(userId, chatId)
        return chatUserEntity?.toChatUser()
    }

    suspend fun getChatsForUser(chatId: Int): Resource<List<ChatUser>> {
        val chatUserEntities = chatUserDao.getChatsForUser(chatId)
        val chatUsers = chatUserEntities.map { it.toChatUser() }
        return Resource.success(chatUsers)
    }

    suspend fun getUsersInChat(chatId: Int): Resource<List<ChatUser>> {
        val chatUserEntities = chatUserDao.getUsersInChat(chatId)
        val chatUsers = chatUserEntities.map { it.toChatUser() }
        return Resource.success(chatUsers)
    }

    suspend fun deleteChatUsersForChatAndUser(chatId: Int, userId: Int) {
        chatUserDao.deleteChatUsersForChatAndUser(chatId, userId)
    }

    suspend fun deleteChatUsersForChat(chatId: Int) {
        chatUserDao.deleteChatUsersForChat(chatId)
    }

    suspend fun deleteChatUsersForUser(userId: Int) {
        chatUserDao.deleteChatUsersForUser(userId)
    }
}
