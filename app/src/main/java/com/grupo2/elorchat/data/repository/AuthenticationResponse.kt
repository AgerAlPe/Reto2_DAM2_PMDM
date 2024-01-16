package com.grupo2.elorchat.data.repository

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthenticationResponse (
    val email: String,
    val token: String,
): Parcelable