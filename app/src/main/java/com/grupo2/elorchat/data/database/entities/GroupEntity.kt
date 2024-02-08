package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grupo2.elorchat.data.Group

@Entity(tableName = "group_table")
class GroupEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")val id: Int? = 0,
    @ColumnInfo(name = "group_name")val name: String,
    @ColumnInfo(name = "is_private")val isPrivate: Boolean,
    @ColumnInfo(name = "remote_id") val remoteId: Int
)
fun GroupEntity.toGroup(): Group {
    return Group(
        id = this.remoteId,
        name = this.name,
        isPrivate = this.isPrivate,
    )
}

fun Group.toGroupEntity(): GroupEntity {
    return GroupEntity(
        id = null,
        name = this.name,
        isPrivate = this.isPrivate,
        remoteId = this.id,
    )
}