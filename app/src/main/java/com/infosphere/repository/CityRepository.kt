package com.infosphere.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.infosphere.models.City
import kotlinx.coroutines.tasks.await

class CityRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val citiesCollection = db.collection("cities")

    suspend fun getAllCities(): Result<List<City>> {
        return try {
            val snapshot = citiesCollection
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()
            val cities = snapshot.toObjects(City::class.java)
            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCityById(cityId: String): Result<City?> {
        return try {
            val document = citiesCollection.document(cityId).get().await()
            val city = document.toObject(City::class.java)
            Result.success(city)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCities(query: String): Result<List<City>> {
        return try {
            val snapshot = citiesCollection
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            val cities = snapshot.toObjects(City::class.java)
            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
