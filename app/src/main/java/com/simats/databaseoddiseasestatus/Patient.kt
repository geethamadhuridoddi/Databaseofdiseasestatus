package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class Patient(
    @SerializedName("id", alternate = ["patient_id", "pk"])
    val id: Int? = null,
    val name: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    @SerializedName("phone", alternate = ["phone_number", "contact"])
    val phone: String? = null,
    val address: String? = null,
    val disease_count: Int? = null,
    @SerializedName("diseases", alternate = ["disease_records", "records", "assigned_diseases", "cases", "disease_list"])
    var diseases: List<Disease>? = null
) {
    val displayPhone: String get() = phone ?: ""
}
