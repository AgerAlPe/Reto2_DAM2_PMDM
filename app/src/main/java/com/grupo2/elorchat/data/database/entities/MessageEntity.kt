package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_table")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "chat_id") val chatId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long?
) {
    fun toEntity(): MessageEntity {
        // Return a new instance with the same properties
        return MessageEntity(id, message, name, userId, chatId, createdAt)
    }
}