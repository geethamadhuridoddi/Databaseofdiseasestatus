package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class CaseRecord(
    @SerializedName("record_id", alternate = ["id", "pk"])
    val recordId: Int? = null,
    @SerializedName("patient_id")
    val patientId: Int? = null,
    @SerializedName("name", alternate = ["patient_name"])
    val patientName: String? = null,
    @SerializedName("age")
    val patientAge: Int? = null,
    @SerializedName("gender")
    val patientGender: String? = null,
    @SerializedName("phone")
    val patientPhone: String? = null,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("disease", alternate = ["disease_name"])
    val diseaseName: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("severity")
    val severity: String? = null,
    @SerializedName("assigned_doctor")
    val doctor: String? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("date", alternate = ["diagnosis_date"])
    val date: String? = null
) {
    fun toPatient(): Patient {
        return Patient(
            id = patientId,
            name = patientName ?: "Unknown",
            age = patientAge ?: 0,
            gender = patientGender ?: "Unknown",
            phone = patientPhone ?: "Unknown",
            address = address ?: "Unknown",
            diseaseCount = 1,
            diseases = listOf(
                Disease(
                    name = diseaseName ?: "Unknown",
                    status = status ?: "Active",
                    severity = severity ?: "Medium",
                    doctorPrimary = doctor,
                    notes = notes,
                    diagnosisDate = date // Mapping date to the diagnosisDate field
                )
            )
        )
    }
}
