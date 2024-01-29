package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date


@Parcelize
class Message (
    val id: Int?,
    val message: String,
    val name: String,
    val userId: Int,
    val chatId: Int,
    var createdAt: String,
): Parcelable