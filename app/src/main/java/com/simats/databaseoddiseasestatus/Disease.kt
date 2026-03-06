package com.simats.databaseoddiseasestatus

import java.util.Date
import java.util.UUID

data class Disease(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val status: String,
    val severity: String,
    val diagnosisDate: Date,
    val assignedDoctor: String,
    val notes: String? = null
)
