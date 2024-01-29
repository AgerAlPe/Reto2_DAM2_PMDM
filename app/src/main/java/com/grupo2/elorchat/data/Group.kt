package com.grupo2.elorchat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Group (
    val id: Int,
    val name: String,
    val isPrivate: Boolean,
    var isUserOnGroup: Boolean = false  // Provide a default value
): Parcelable {

    override fun toString(): String {
        return "Group(id=$id, name='$name', isPrivate=$isPrivate, isUserOnGroup=$isUserOnGroup)"
    }
}