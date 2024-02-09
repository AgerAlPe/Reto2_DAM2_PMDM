package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Group (
    val id: Int,
    val name: String,
    val isPrivate: Boolean
): Parcelable {

}