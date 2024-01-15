package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.utils.Resource
import com.grupo2.elorchat.data.repository.AuthenticationResponse

interface CommonUserRepository {
    suspend fun loginUser(user: LoginUser) : Resource<AuthenticationResponse>

}