package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class RemoteGroupDataSource: BaseDataSource(), CommonGroupRepository {
    override suspend fun getGroups() = getResult {
        RetrofitClient.apiInterface.getGroups()
    }

    override suspend fun createGroup(group : Group) = getResult {
        RetrofitClient.apiInterface.createGroup(group)
    }
    
    override suspend fun getMessagesFromGroup(groupId : Int) = getResult {
        RetrofitClient.apiInterface.getMessagesFromGroup(groupId)
    }

    override suspend fun getUserGroups(userId: Int) = getResult {
        RetrofitClient.apiInterface.getUserGroups(userId)
    }
}