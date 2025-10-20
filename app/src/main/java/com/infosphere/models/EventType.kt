package com.infosphere.models

import com.google.firebase.firestore.DocumentId

data class EventType(
    @DocumentId
    val id: String = "",
    val name: String = "",
) {
    // Empty constructor for Firebase
    constructor() : this("", "")
}
