package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id : Int?,
    val name: String,
    val surnames: String,
    val email: String,
    val direction: String,
    val phoneNumber: String,
    val fctDual: Boolean,
    val password: String,
    val roles: List<Role>,
    val dni: String,
): Parcelable