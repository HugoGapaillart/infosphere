package com.infosphere.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.infosphere.models.EventType
import kotlinx.coroutines.tasks.await

class EventTypeRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val eventTypesCollection = db.collection("eventTypes")

    suspend fun getAllEventTypes(): Result<List<EventType>> {
        return try {
            val snapshot = eventTypesCollection
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()
            val eventTypes = snapshot.toObjects(EventType::class.java)
            Result.success(eventTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventTypeById(typeId: String): Result<EventType?> {
        return try {
            val document = eventTypesCollection.document(typeId).get().await()
            val eventType = document.toObject(EventType::class.java)
            Result.success(eventType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
