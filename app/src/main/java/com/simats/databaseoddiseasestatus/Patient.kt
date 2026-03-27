package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class Patient(
    @SerializedName("id", alternate = ["patient_id", "pk"])
    val id: Int? = null,
    val name: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    @SerializedName("phone", alternate = ["phone_number", "contact", "mobile"])
    val phone: String? = null,
    val address: String? = null,
    @SerializedName("disease_count", alternate = ["diseases_count", "total_diseases", "count", "num_diseases", "total", "disease_name", "num_cases", "disease_count_total"])
    val diseaseCount: Int? = null,
    @SerializedName("diseases", alternate = ["disease_records", "records", "assigned_diseases", "cases", "disease_list", "patient_diseases"])
    var diseases: List<Disease>? = null
) {
    val displayPhone: String get() = phone ?: ""
}
