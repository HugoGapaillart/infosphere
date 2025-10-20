package com.infosphere.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val selectedCityIds: List<String> = emptyList(), // Cities the user is interested in
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", emptyList(), Timestamp.now(), Timestamp.now())
}
