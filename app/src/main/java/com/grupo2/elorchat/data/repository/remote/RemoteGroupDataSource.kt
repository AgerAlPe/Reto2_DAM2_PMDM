package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.CommonGroupRepository

class RemoteGroupDataSource: BaseDataSource(), CommonGroupRepository {
    override suspend fun getGroups() = getResult {
        RetrofitClient.apiInterface.getGroups()
    }

    override suspend fun createGroup(group : Group) = getResult {
        RetrofitClient.apiInterface.createGroup(group)
    }
    
    override suspend fun getAllMessages() = getResult {
        RetrofitClient.apiInterface.getAllMessages()
    }
}