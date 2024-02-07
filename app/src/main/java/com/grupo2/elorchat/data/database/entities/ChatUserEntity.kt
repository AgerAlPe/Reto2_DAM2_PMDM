package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grupo2.elorchat.data.ChatUser
import com.grupo2.elorchat.data.Group

@Entity(tableName = "chat_user")
class ChatUserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "chat_id") val chatId: Int,
    @ColumnInfo(name = "isAdmin") val isAdmin: Boolean
)

fun ChatUserEntity.toChatUser(): ChatUser {
    return ChatUser(
        id = this.id,
        userId = this.userId,
        chatId = this.chatId,
        isAdmin = this.isAdmin
    )
}

fun ChatUser.toChatUserEntity(): ChatUserEntity {
    return ChatUserEntity(
        id = this.id,
        userId = this.userId,
        chatId = this.chatId,
        isAdmin = this.isAdmin
    )
}
