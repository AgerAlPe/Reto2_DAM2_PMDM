package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.repository.CommonGroupRepository

class RemoteGroupDataSource: BaseDataSource(), CommonGroupRepository {
    override suspend fun getGroups() = getResult {
        RetrofitClient.apiInterface.getGroups()
    }

}