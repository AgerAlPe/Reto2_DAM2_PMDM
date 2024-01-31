package com.grupo2.elorchat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.grupo2.elorchat.data.Role
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.database.GroupListConverter
import com.grupo2.elorchat.data.database.RoleListConverter

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int? = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "surnames") val surnames: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "direction") val direction: String,
    @ColumnInfo(name = "phoneNumber") val phoneNumber: String,
    @ColumnInfo(name = "fctDual") val fctDual: Boolean,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "dni") val dni: String,
    @TypeConverters(RoleListConverter::class)
    @ColumnInfo(name = "roles") val roles: List<Role>
)

fun UserEntity.toUser(): User {
    return User(
        id = this.id,
        name = this.name,
        surnames = this.surnames,
        email = this.email,
        direction = this.direction,
        phoneNumber = this.phoneNumber,
        fctDual = this.fctDual,
        password = this.password,
        dni = this.dni,
        roles = this.roles
    )
}

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = this.id,
        name = this.name,
        surnames = this.surnames,
        email = this.email,
        direction = this.direction,
        phoneNumber = this.phoneNumber,
        fctDual = this.fctDual,
        password = this.password,
        dni = this.dni,
        roles = this.roles
    )
}
