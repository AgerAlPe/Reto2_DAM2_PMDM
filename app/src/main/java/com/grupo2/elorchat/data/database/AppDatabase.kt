package com.grupo2.elorchat.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grupo2.elorchat.data.database.dao.ChatUserDao
import com.grupo2.elorchat.data.database.dao.GroupDao
import com.grupo2.elorchat.data.database.dao.MessageDao
import com.grupo2.elorchat.data.database.dao.UserDao
import com.grupo2.elorchat.data.database.entities.ChatUserEntity
import com.grupo2.elorchat.data.database.entities.GroupEntity
import com.grupo2.elorchat.data.database.entities.MessageEntity
import com.grupo2.elorchat.data.database.entities.UserEntity

@Database(entities = [UserEntity::class, MessageEntity::class, GroupEntity::class, ChatUserEntity::class], version = 3, exportSchema = false)
@TypeConverters(RoleListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getChatUserDao(): ChatUserDao
    abstract fun getUserDao(): UserDao
    abstract fun getMessageDao(): MessageDao
    abstract fun getGroupDao(): GroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Handle schema changes during development
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}