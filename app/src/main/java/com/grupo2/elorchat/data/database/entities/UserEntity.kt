package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.grupo2.elorchat.data.Role
import com.grupo2.elorchat.data.database.RoleListConverter

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "email") val email: String,
    @TypeConverters(RoleListConverter::class)
    @ColumnInfo(name = "roles") val roles: List<Role>
)
