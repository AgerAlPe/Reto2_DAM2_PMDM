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

    suspend fun insertChatUsers(chatUsers: List<ChatUser>) {
        val chatUserEntities = chatUsers.map { it.toChatUserEntity() }
        chatUserDao.insertChatUsers(chatUserEntities)
    }

    suspend fun getChatUser(userId: Int, chatId: Int): ChatUser? {
        val chatUserEntity = chatUserDao.getChatUser(userId, chatId)
        return chatUserEntity?.toChatUser()
    }

    suspend fun getChatsForUser(chatId: Int): List<ChatUser> {
        val chatUserEntities = chatUserDao.getChatsForUser(chatId)
        return chatUserEntities.map { it.toChatUser() }
    }

    suspend fun getUserChats(userId: Int): List<ChatUser> {
        val chatUserEntities = chatUserDao.getUserChats(userId)
        return chatUserEntities.map { it.toChatUser() }
    }

    suspend fun deleteUserFromGroup(chatId: Int, userId: Int) {
        chatUserDao.deleteUserFromGroup(chatId, userId)
    }

    suspend fun deleteAllUserFromGroup(chatId: Int) {
        chatUserDao.deleteAllUserFromGroup(chatId)
    }

    suspend fun deleteUserFromAllGroups(userId: Int) {
        chatUserDao.deleteUserFromAllGroups(userId)
    }
}
