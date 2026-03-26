package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName

data class Disease(
    @SerializedName("record_id", alternate = ["id", "pk"])
    val recordId: Int? = null,

    @SerializedName("disease_id")
    val diseaseId: Int? = null,

    @SerializedName("disease_name", alternate = ["disease", "name"])
    val name: String? = null,

    @SerializedName("status")
    val status: String = "Active",

    @SerializedName("severity")
    val severity: String = "Medium",

    @SerializedName("diagnosis_date")
    val diagnosisDate: String? = null,

    @SerializedName("doctor", alternate = ["assigned_doctor", "doctor_name", "primary_doctor"])
    val doctorPrimary: String? = null,

    @SerializedName("assigned_doctor_secondary")
    val doctorSecondary: String? = null,

    @SerializedName("notes", alternate = ["disease_notes", "patient_notes", "remarks", "comments"])
    val notes: String? = null,

    @SerializedName("local_id")
    val providedLocalId: String? = null,

    @Transient
    var explicitDoctor: String? = null
) {
    val assignedDoctor: String 
        get() {
            if (!explicitDoctor.isNullOrBlank()) return explicitDoctor!!
            if (!doctorPrimary.isNullOrBlank()) return doctorPrimary.trim()
            if (!doctorSecondary.isNullOrBlank()) return doctorSecondary.trim()
            return ""
        }

    val localId: String 
        get() = providedLocalId ?: recordId?.toString() ?: name ?: "unknown_disease"
}
