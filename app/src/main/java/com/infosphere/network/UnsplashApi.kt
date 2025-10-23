package com.infosphere.network

import com.infosphere.models.UnsplashPhoto
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {
    // Returns a single photo by default. Use count to request multiple.
    @GET("photos/random")
    suspend fun getRandomPhoto(
        @Query("query") query: String? = null,
        @Query("orientation") orientation: String? = null
    ): UnsplashPhoto

    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Query("query") query: String? = null,
        @Query("orientation") orientation: String? = null,
        @Query("count") count: Int
    ): List<UnsplashPhoto>
}

