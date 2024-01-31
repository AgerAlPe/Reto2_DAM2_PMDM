package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.utils.Resource

import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

interface CommonSocketRepository {

    suspend fun joinRoom(room : String, userId : Int) : Resource<Void>

    suspend fun leaveRoom(room : String, userId : Int) : Resource<Void>

}