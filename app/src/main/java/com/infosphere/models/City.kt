package com.infosphere.models

import com.google.firebase.firestore.DocumentId

data class City(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val region: String = ""
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", "")
}
