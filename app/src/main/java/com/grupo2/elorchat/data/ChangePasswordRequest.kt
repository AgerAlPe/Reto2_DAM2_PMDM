package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChangePasswordRequest (
    val email: String,
    val password: String,
    val newpassword: String
): Parcelable