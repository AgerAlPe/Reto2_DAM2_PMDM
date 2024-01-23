package com.grupo2.elorchat.data.repository

import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.RegisterUser
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.utils.Resource

interface CommonUserRepository {
    suspend fun loginUser(user: LoginUser) : Resource<AuthenticationResponse>

    suspend fun getUserByEmail(userEmail : String) : Resource<User>

    suspend fun updateRegisteredUser(userId : Int, user: RegisterUser) : Resource<Int>
}