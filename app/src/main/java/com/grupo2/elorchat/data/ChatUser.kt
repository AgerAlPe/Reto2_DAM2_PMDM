package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatUser (
    val id: Int,
    val userId: Int,
    val chatId: Int,
    val admin: Boolean
): Parcelable {
    constructor(userId: Int, chatId: Int, admin: Boolean) : this(0, userId, chatId, admin)

    constructor(userId: Int, chatId: Int) : this(0, userId, chatId, false)
}