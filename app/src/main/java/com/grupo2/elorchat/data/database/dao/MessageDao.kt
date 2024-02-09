package com.grupo2.elorchat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grupo2.elorchat.data.database.entities.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM message_table")
    suspend fun getAllMessage():List<MessageEntity>

    @Query("SELECT * FROM message_table where id = :userId")
    suspend fun getAllUserMessage(userId:Int):List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(message: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(messageEntity: MessageEntity)

    @Query("SELECT * FROM message_table WHERE chat_id = :groupId")
    suspend fun getAllGroupMessages(groupId: Int): List<MessageEntity>
}