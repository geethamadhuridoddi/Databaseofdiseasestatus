package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class Patient(
    val id: String? = null,
    val name: String,
    val age: Int,
    val gender: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val address: String,
    val diseases: MutableList<Disease>? = null
)
