package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_table")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")val id: Int = 0,
    @ColumnInfo(name = "room")val room: String,
    @ColumnInfo(name = "text")val text: String,
    @ColumnInfo(name = "authorName")val authorName: String)