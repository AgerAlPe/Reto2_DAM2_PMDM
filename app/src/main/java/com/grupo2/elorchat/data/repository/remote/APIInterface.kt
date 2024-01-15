package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.repository.AuthenticationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST



interface APIInterface {
    @POST("login")
    suspend fun loginUser(@Body user: LoginUser) : Response<AuthenticationResponse>
}