package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.utils.Resource

interface CommonSocketRepository {

    suspend fun joinRoom(room : String, userId : Int) : Resource<Void>
}