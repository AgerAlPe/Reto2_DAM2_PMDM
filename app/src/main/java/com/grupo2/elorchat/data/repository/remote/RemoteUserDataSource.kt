package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.repository.CommonUserRepository

class RemoteUserDataSource: BaseDataSource(), CommonUserRepository {
    override suspend fun loginUser(user: LoginUser) = getResult {
        RetrofitClient.apiInterface.loginUser(user)
    }


}