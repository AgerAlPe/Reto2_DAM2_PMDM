package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Response

@Parcelize
class ChatUserMovementResponse (
    val response: String,
): Parcelable