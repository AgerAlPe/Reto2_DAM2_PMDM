package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.Message
import com.grupo2.elorchat.data.User
import com.grupo2.elorchat.data.RegisterUser
import com.grupo2.elorchat.data.repository.AuthenticationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface APIInterface {
    @POST("auth/login")
    suspend fun loginUser(@Body user: LoginUser) : Response<AuthenticationResponse>

    @GET("chats")
    suspend fun getGroups(): Response<List<Group>>

    @GET("users/byEmail/{email}")
    suspend fun getUserByEmail(@Path("email") userEmail : String): Response<User>

    @PUT("users/{id}")
    suspend fun updateRegisteredUser(@Path("id") userId : Int, @Body user: RegisterUser): Response<Int>

    @POST("chats")
    suspend fun createGroup(@Body name : String, isPrivate : Boolean): Response<Int>

    @GET("messages")
    suspend fun getAllMessages() : Response<List<Message>>
}