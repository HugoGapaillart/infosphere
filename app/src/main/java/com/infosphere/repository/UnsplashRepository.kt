package com.infosphere.repository

import com.infosphere.models.UnsplashPhoto
import com.infosphere.network.UnsplashApi
import com.infosphere.network.UnsplashClient

class UnsplashRepository(
    private val api: UnsplashApi = UnsplashClient.api
) {
    suspend fun getRandomPhoto(query: String? = null, orientation: String? = null): UnsplashPhoto? {
        return try {
            api.getRandomPhoto(query = query, orientation = orientation)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRandomPhotos(query: String? = null, orientation: String? = null, count: Int = 1): List<UnsplashPhoto> {
        return try {
            api.getRandomPhotos(query = query, orientation = orientation, count = count)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
