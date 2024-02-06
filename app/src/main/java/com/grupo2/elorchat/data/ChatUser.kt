package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatUser (
    val id: Int,
    val userId: Int,
    val chatId: Int,
    val isAdmin: Boolean
): Parcelable {
    constructor(id: Int, userId: Int, chatId: Int) : this(id, userId, chatId, false)
}