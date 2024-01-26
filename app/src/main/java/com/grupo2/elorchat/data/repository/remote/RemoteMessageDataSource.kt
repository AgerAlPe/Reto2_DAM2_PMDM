package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.repository.CommonMessageRepository

class RemoteMessageDataSource : BaseDataSource(), CommonMessageRepository  {

    override suspend fun getMessages() = getResult {
        RetrofitClient.apiInterface.getMessages()
    }

}