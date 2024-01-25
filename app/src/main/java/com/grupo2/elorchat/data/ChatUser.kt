package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatUser (
    val userId: Int,
    val chatId: Int,
    val admin: Boolean
): Parcelable {
    constructor(userId: Int, chatId: Int) : this(userId, chatId, false)

}