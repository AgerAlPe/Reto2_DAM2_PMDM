package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.repository.CommonSocketRepository
import com.grupo2.elorchat.utils.Resource

class RemoteSocketDataSource: BaseDataSource(), CommonSocketRepository {

    //    override suspend fun joinRoom(room: String, userId: Int) = getResult {
//        RetrofitClient.apiInterface.joinRoomSocket(room, userId)
//    }
    override suspend fun joinRoom(room: String, userId: Int): Resource<Void> {
        TODO("Not yet implemented")
    }

}