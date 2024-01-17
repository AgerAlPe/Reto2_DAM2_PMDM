package com.grupo2.elorchat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Proveo Retrofit
    //@Provides
    //fun provideRetrofit():Retrofit{
    //return Retrofit.Builder()
    //            .client(client)
    //            .baseUrl(API_URI)
    //            .addConverterFactory(GsonConverterFactory.create())
    //            .build
    //}
}