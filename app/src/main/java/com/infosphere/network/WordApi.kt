package com.infosphere.network

import com.infosphere.models.RemoteWord
import retrofit2.http.GET
import retrofit2.http.Path

interface WordApi {
    @GET("api/daily")
    suspend fun getDailyWord(): RemoteWord

    @GET("api/weekly")
    suspend fun getWeeklyWord(): RemoteWord

    @GET("api/monthly")
    suspend fun getMonthlyWord(): RemoteWord

    @GET("api/size/{size}")
    suspend fun getRandomWordsBySize(@Path("size") size: Int): List<RemoteWord>

    @GET("api/sizemax/{max}")
    suspend fun getRandomWordsByMaxSize(@Path("max") max: Int): List<RemoteWord>
}
