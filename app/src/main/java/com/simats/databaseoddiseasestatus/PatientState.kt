package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

val globalPatients = mutableStateListOf<Patient>(
    Patient(
        id = 1,
        name = "Alice Johnson",
        age = 45,
        gender = "Female",
        phone = "+1 555 0101",
        address = "123 Main St, New York, NY 10001",
        disease_count = 2,
        diseases = mutableListOf(
            Disease(
                name = "Type 2 Diabetes",
                status = "Active",
                severity = "Medium",
                diagnosisDate = dateFormat.format(Date()),
                doctorPrimary = "Dr. Smith",
                notes = "Blood sugar monitoring required daily"
            ),
            Disease(
                name = "Hypertension",
                status = "Recovering",
                severity = "High",
                diagnosisDate = dateFormat.format(Date()),
                doctorPrimary = "Dr. Johnson",
                notes = "Weekly check-ups"
            )
        )
    ),
    Patient(
        id = 2,
        name = "Bob Smith",
        age = 62,
        gender = "Male",
        phone = "+1 555 0102",
        address = "456 Oak Ave, Los Angeles, CA 90001",
        disease_count = 1,
        diseases = mutableListOf(
            Disease(
                name = "Arthritis",
                status = "Active",
                severity = "Low",
                diagnosisDate = dateFormat.format(Date()),
                doctorPrimary = "Dr. Williams",
                notes = "Pain management as needed"
            )
        )
    ),
    Patient(
        id = 3,
        name = "Carol Williams",
        age = 28,
        gender = "Female",
        phone = "+1 555 0103",
        address = "789 Pine St, Chicago, IL 60601",
        disease_count = 0,
        diseases = mutableListOf()
    )
)
