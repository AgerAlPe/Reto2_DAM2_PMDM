package com.grupo2.elorchat.data.repository.remote

import com.grupo2.elorchat.ElorChat
import com.grupo2.elorchat.utils.ssl.MyHostnameVerifier
import com.grupo2.elorchat.utils.ssl.trustManager
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

object RetrofitClient {

    // esta es la ip a la que vamos a hacer peticiones (es localhost pero desde el emulador no deja)
    // NOTE acordarse de que hace falta actualizar el manifest
  

    private const val API_URI = "http://10.5.7.37:8080/api/"
    val sslContext = prueba()

    var client = OkHttpClient.Builder().addInterceptor { chain ->
        val authToken = ElorChat.userPreferences.fetchAuthToken()
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $authToken")
            .build()
        chain.proceed(newRequest)
    }
        .hostnameVerifier(MyHostnameVerifier())
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .build()

    // creamos el cliente de retrofit con la url de la api
    val retrofitClient: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl(API_URI)
            .addConverterFactory(GsonConverterFactory.create())
    }

    val apiInterface: APIInterface by lazy {
        retrofitClient
            .build()
            .create(APIInterface::class.java)
    }

    fun prueba(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
        return sslContext
    }
}