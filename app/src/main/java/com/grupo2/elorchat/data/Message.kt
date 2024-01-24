package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize



@Parcelize
class Message (
    val id: Int?,
    val message: String,
    val name: String,
    val userId: Int,
    val chatId: Int,
    val createdAt: Long?,
): Parcelable