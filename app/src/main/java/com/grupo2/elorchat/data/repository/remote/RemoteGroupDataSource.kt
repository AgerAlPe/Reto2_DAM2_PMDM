package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.repository.CommonGroupRepository
import com.grupo2.elorchat.utils.Resource

class RemoteGroupDataSource: BaseDataSource(), CommonGroupRepository {
    override suspend fun getGroups() = getResult {
        RetrofitClient.apiInterface.getGroups()
    }

    override suspend fun createGroup(name : String, isPrivate : Boolean) = getResult {
        RetrofitClient.apiInterface.createGroup(name, isPrivate)
    }
}