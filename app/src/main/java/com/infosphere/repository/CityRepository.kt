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

    suspend fun addCityIfNotExists(city: String, region: String, country: String) {
        val db = FirebaseFirestore.getInstance()
        val citiesCollection = db.collection("cities")

        val existing = citiesCollection
            .whereEqualTo("name", city)
            .whereEqualTo("region", region)
            .whereEqualTo("country", country)
            .get()
            .await()

        if (existing.isEmpty) {
            // Ajoute la ville si elle n’existe pas
            citiesCollection.add(
                mapOf(
                    "name" to city,
                    "region" to region,
                    "country" to country
                )
            ).await()
        }
    }
}
