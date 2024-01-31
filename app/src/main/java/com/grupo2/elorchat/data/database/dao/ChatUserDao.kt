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

    @Query("SELECT * FROM chat_user WHERE user_id = :userId AND chat_id = :chatId")
    suspend fun getChatUser(userId: Int, chatId: Int): ChatUserEntity?

    @Query("DELETE FROM chat_user WHERE user_id = :userId AND chat_id = :chatId")
    suspend fun deleteChatUser(userId: Int, chatId: Int)

    @Query("SELECT * FROM chat_user WHERE chat_id = :chatId")
    suspend fun getChatUsersInChat(chatId: Int): List<ChatUserEntity>
}