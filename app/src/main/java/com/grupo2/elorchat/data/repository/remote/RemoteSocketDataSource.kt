package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.repository.CommonSocketRepository
import com.grupo2.elorchat.utils.Resource

class RemoteSocketDataSource: BaseDataSource(), CommonSocketRepository {

    override suspend fun joinRoom(room: String, isAdmin: Boolean) = getResult {
        RetrofitClient.apiInterface.joinRoomSocket(room, isAdmin)
    }

    override suspend fun leaveRoom(room: String) = getResult {
        RetrofitClient.apiInterface.leaveRoomSocket(room)
    }

}