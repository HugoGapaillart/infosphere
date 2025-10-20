package com.infosphere.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.infosphere.models.Event
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val eventsCollection = db.collection("events")
    private val storageRef = storage.reference.child("event_photos")

    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val docRef = eventsCollection.add(event).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadEventPhoto(eventId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "${eventId}_${UUID.randomUUID()}.jpg"
            val photoRef = storageRef.child(fileName)
            
            photoRef.putFile(imageUri).await()
            val downloadUrl = photoRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvent(eventId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("updatedAt", Timestamp.now())
            }
            eventsCollection.document(eventId).update(updatesWithTimestamp).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvent(eventId: String): Result<Event?> {
        return try {
            val document = eventsCollection.document(eventId).get().await()
            val event = document.toObject(Event::class.java)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUpcomingEventsByCities(cityIds: List<String>): Flow<List<Event>> = callbackFlow {
        if (cityIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val currentTime = Timestamp.now()
        val listener = eventsCollection
            .whereIn("cityId", cityIds)
            .whereGreaterThanOrEqualTo("date", currentTime)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun getEventsByUser(userId: String): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .whereEqualTo("createdBy", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun getEventsByCities(cityIds: List<String>): Flow<List<Event>> {
        return callbackFlow {
            val query = eventsCollection
                .whereIn("cityId", cityIds)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(events)
            }

            awaitClose { listener.remove() }
        }
    }


    suspend fun searchEvents(
        cityId: String? = null,
        eventTypes: List<String>? = null
    ): Result<List<Event>> {
        return try {
            var query: Query = eventsCollection
                .whereGreaterThanOrEqualTo("date", Timestamp.now())
                .orderBy("date", Query.Direction.ASCENDING)

            if (cityId != null) {
                query = query.whereEqualTo("cityId", cityId)
            }

            val snapshot = query.get().await()
            var events = snapshot.toObjects(Event::class.java)

            // Filter by event types if provided (client-side filtering)
            if (eventTypes != null && eventTypes.isNotEmpty()) {
                events = events.filter { event ->
                    event.eventTypes.any { it in eventTypes }
                }
            }

            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
