package com.grupo2.elorchat.di

import android.content.Context
import androidx.room.Room
import com.grupo2.elorchat.data.database.MessageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private const val  MESSAGE_DATABASE_NAME = "message_database"

    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, MessageDatabase::class.java, MESSAGE_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideMessageDao(db:MessageDatabase) = db.getMessageDao()
}