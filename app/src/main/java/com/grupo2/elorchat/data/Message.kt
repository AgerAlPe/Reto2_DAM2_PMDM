package com.grupo2.elorchat.data

import android.os.Parcelable
import com.grupo2.elorchat.data.database.entities.MessageEntity
import kotlinx.parcelize.Parcelize



@Parcelize
class Message (
    val id: Int?,
    val message: String,
    val name: String,
    val userId: Int,
    val chatId: Int,
    val createdAt: Long?,
): Parcelable {
    fun toEntity(): MessageEntity {
        return MessageEntity(
            id = id ?: 0, // Provide a default value if id is nullable
            message = this.message,
            name = name,
            userId = userId,
            chatId = chatId,
            createdAt = createdAt ?: 0
        )
    }
}