package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.database.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "message_table")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int? = 0,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "chat_id") val chatId: Int,
    @ColumnInfo(name = "created_at") val createdAt: String
)

suspend fun MessageEntity.toMessage(userRepository: UserRepository): Message {
    val user = userRepository.getUser(this.userId)

    return Message(
        id = this.id,
        message = this.message,
        user = user,
        chatId = this.chatId,
        createdAt = this.createdAt
    )
}

fun Message.toMessageEntity(): MessageEntity {
    return MessageEntity(
        id = this.id,
        message = this.message,
        userId = this.user.id!!,
        chatId = this.chatId,
        createdAt = getCurrentFormattedDate()
    )
}

private fun getCurrentFormattedDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}