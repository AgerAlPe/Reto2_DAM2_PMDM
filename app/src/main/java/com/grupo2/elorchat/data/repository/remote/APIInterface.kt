package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.data.Group
import com.grupo2.elorchat.data.LoginUser
import com.grupo2.elorchat.data.repository.AuthenticationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST



interface APIInterface {
    @POST("login")
    suspend fun loginUser(@Body user: LoginUser) : Response<AuthenticationResponse>

    //ESTA PARTE ESTA SIN CONECTAR, YA QUE NO SE SABE A QUE SECCIÓN VA
    //HASTA NUEVO AVISO NO TOCAR
    @GET("UNA URL MU' LOCA")
    suspend fun getGroups(@Body group: Group) : Response<List<Group>>
}