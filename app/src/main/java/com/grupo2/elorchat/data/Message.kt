package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date


@Parcelize
class Message (
    val id: Int?,
    val message: String,
    val user: User,
    val chatId: Int,
    val createdAt: String,
): Parcelable