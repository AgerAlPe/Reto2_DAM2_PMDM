package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")val id: Int = 0,
    @ColumnInfo(name = "name")val name: String,
    @ColumnInfo(name = "surnames")val surnames: String,
    @ColumnInfo(name = "dni")val dni: String,
    @ColumnInfo(name = "email")val email: String,
    @ColumnInfo(name = "password")val password: String
)
