package com.infosphere.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://trouve-mot.fr/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: WordApi by lazy {
        retrofit.create(WordApi::class.java)
    }
}

