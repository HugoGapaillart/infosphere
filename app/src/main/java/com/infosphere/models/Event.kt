package com.infosphere.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: Timestamp = Timestamp.now(),
    val photoUrls: List<String> = emptyList(),
    val cityId: String = "",
    val cityName: String = "",
    val eventTypes: List<String> = emptyList(), // List of type IDs
    val createdBy: String = "", // User ID
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "", Timestamp.now(), emptyList(), "", "", emptyList(), "", Timestamp.now(), Timestamp.now())
    
    fun isPast(): Boolean {
        return date.toDate().time < System.currentTimeMillis()
    }
}
