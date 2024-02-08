package com.grupo2.elorchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grupo2.elorchat.data.database.entities.ChatUserEntity

@Dao
interface ChatUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatUser(chatUser: ChatUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatUsers(chatUserEntities: List<ChatUserEntity>)

    @Query("SELECT * FROM chat_user WHERE user_id = :userId AND chat_id = :chatId")
    suspend fun getChatUser(userId: Int, chatId: Int): ChatUserEntity?

    @Query("SELECT * FROM chat_user WHERE chat_id = :chatId")
    suspend fun getChatsForUser(chatId: Int): List<ChatUserEntity>

    @Query("SELECT * FROM chat_user WHERE user_id = :userId")
    suspend fun getUserChats(userId: Int): List<ChatUserEntity>

    @Query("DELETE FROM chat_user WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun deleteUserFromGroup(chatId: Int, userId: Int)

    @Query("DELETE FROM chat_user WHERE chat_id = :chatId")
    suspend fun deleteAllUserFromGroup(chatId: Int)

    @Query("DELETE FROM chat_user WHERE user_id = :userId")
    suspend fun deleteUserFromAllGroups(userId: Int)
}