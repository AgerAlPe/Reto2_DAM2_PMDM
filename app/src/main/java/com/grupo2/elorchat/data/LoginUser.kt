package com.grupo2.elorchat.data

import android.companion.AssociatedDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginUser(
    val email: String,
    val password: String,
   // val device: String,
): Parcelable