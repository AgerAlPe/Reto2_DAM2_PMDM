package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.ChangePasswordRequest
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.RegisterUser
import com.grupo2.elorchat.data.repository.CommonUserRepository
import com.grupo2.elorchat.utils.Resource

class RemoteUserDataSource: BaseDataSource(), CommonUserRepository {
    override suspend fun loginUser(user: LoginUser) = getResult {
        RetrofitClient.apiInterface.loginUser(user)
    }

    override suspend fun getUserByEmail(userEmail: String) = getResult {
        RetrofitClient.apiInterface.getUserByEmail(userEmail)
    }

    override suspend fun updateRegisteredUser(userId: Int, user: RegisterUser) = getResult {
        RetrofitClient.apiInterface.updateRegisteredUser(userId, user)
    }


}