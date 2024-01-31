package com.grupo2.elorchat.di

import android.content.Context
import androidx.room.Room
import com.grupo2.elorchat.data.database.AppDatabase
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.data.repository.CommonSocketRepository
import com.grupo2.elorchat.data.repository.remote.RemoteGroupDataSource
import com.grupo2.elorchat.data.repository.remote.RemoteSocketDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private const val APP_DATABASE_NAME = "app_database"

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
        return RemoteGroupDataSource() // Change this according to your real implementation
    }

    @Provides
    @Singleton
    fun provideCommonSocketRepository(): CommonSocketRepository {
        return RemoteSocketDataSource() // Change this according to your real implementation
    }

    @Singleton
    @Provides
    fun provideUserDao(db: AppDatabase) = db.getUserDao()

    @Singleton
    @Provides
    fun provideMessageDao(db: AppDatabase) = db.getMessageDao()

    @Singleton
    @Provides
    fun provideGroupDao(db: AppDatabase) = db.getGroupDao()

    @Provides
    @Singleton
    fun provideGroupName(): String {
        // You can return the actual value or retrieve it from a source.
        return "YourGroupName"
    }


}
