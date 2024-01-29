package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatUserEmailRequest (
    val email: String,
    val chatId: Int,
): Parcelable