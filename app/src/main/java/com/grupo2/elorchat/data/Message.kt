package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Message (
    val id: Int,
    val text: String,
    val userId: Int,
    val groupId: Int,
): Parcelable