package com.grupo2.elorchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.dao.UserDao
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.database.entities.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase:RoomDatabase() {
    abstract fun getUserDao():UserDao
    abstract fun getMessageDao():MessageDao
}