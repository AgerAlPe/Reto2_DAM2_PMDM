package com.grupo2.elorchat.di

import android.content.Context
import androidx.room.Room
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private const val  APP_DATABASE_NAME = "app_database"

    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            APP_DATABASE_NAME
        ).build()

    @Provides
    @Singleton
    fun provideCommonGroupRepository(): CommonGroupRepository {
        return RemoteGroupDataSource() // Cambia esto según tu implementación real
    }

    @Singleton
    @Provides
    fun provideUserDao(db:AppDatabase) = db.getUserDao()

    @Singleton
    @Provides
    fun provideMessageDao(db:AppDatabase) = db.getMessageDao()
}