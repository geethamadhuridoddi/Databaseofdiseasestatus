package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class ActivityLogItem(
    val id: Int? = null,
    val message: String,
    @SerializedName("timestamp")
    val timestamp: String? = null
)
