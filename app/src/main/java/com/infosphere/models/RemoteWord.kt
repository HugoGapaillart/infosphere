package com.infosphere.models

import com.google.gson.annotations.SerializedName

class RemoteWord (
    @SerializedName("name") val name: String,
    @SerializedName("categorie") val categorie: String
)