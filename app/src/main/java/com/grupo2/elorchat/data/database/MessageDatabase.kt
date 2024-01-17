package com.grupo2.elorchat.data.database

import androidx.room.Database
import androidx.room.Entity
import androidx.room.RoomDatabase
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.entities.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class MessageDatabase:RoomDatabase() {

    abstract fun getMessageDao():MessageDao
}