package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class Patient(
    @SerializedName("id", alternate = ["patient_id", "pk"])
    val id: Int? = null,
    @SerializedName("name", alternate = ["patient_name", "username", "user_name", "display_name"])
    val name: String? = null,
    @SerializedName("age", alternate = ["patient_age", "user_age", "years"])
    val age: Int? = null,
    @SerializedName("gender", alternate = ["patient_gender", "sex", "user_gender"])
    val gender: String? = null,
    @SerializedName("phone", alternate = ["phone_number", "contact", "mobile", "patient_phone"])
    val phone: String? = null,
    @SerializedName("address", alternate = ["patient_address", "location", "residence"])
    val address: String? = null,
    @SerializedName("disease_count", alternate = ["diseases_count", "total_diseases", "count", "num_diseases", "total", "disease_name", "num_cases", "disease_count_total", "case_count", "active_cases_count"])
    val diseaseCount: Int? = null,
    @SerializedName("diseases", alternate = ["disease_records", "records", "assigned_diseases", "cases", "disease_list", "patient_diseases"])
    var diseases: List<Disease>? = null
) {
    val displayPhone: String get() = phone ?: ""
}
