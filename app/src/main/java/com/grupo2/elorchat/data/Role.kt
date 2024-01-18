package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Role (
    val id: Int,
    val name: String,
): Parcelable