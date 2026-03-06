package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.mutableStateListOf
import com.google.gson.annotations.SerializedName

data class Notification(
    val id: Int,
    val message: String,
    val type: String? = null,
    @SerializedName("is_read")
    var isRead: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null
)

val globalNotifications = mutableStateListOf<Notification>()
