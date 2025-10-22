package com.infosphere.repository

import com.infosphere.models.RemoteWord
import com.infosphere.network.WordApi

class WordRepository(
    private val api: WordApi
) {
    suspend fun getDailyWord(): RemoteWord? {
        return try {
            api.getDailyWord()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getWeeklyWord(): RemoteWord? {
        return try {
            api.getWeeklyWord()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMonthlyWord(): RemoteWord? {
        return try {
            api.getMonthlyWord()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRandomWordByMaxSize(max: Int): RemoteWord? {
        return try {
            val list = api.getRandomWordsByMaxSize(max)
            if (list.isEmpty()) null else list.random()
        } catch (e: Exception) {
            null
        }
    }
}
