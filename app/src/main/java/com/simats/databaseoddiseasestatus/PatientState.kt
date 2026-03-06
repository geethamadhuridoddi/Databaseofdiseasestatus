package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.mutableStateListOf
import java.util.Date

val globalPatients = mutableStateListOf<Patient>(
    Patient(
        id = "YJ-555 0101",
        name = "Alice Johnson",
        age = 45,
        gender = "Female",
        phoneNumber = "+1 555 0101",
        address = "123 Main St, New York, NY 10001",
        diseases = mutableListOf(
            Disease(
                name = "Type 2 Diabetes",
                status = "Active",
                severity = "Medium",
                diagnosisDate = Date(),
                assignedDoctor = "Dr. Smith",
                notes = "Blood sugar monitoring required daily"
            ),
            Disease(
                name = "Hypertension",
                status = "Recovering",
                severity = "High",
                diagnosisDate = Date(),
                assignedDoctor = "Dr. Johnson",
                notes = "Weekly check-ups"
            )
        )
    ),
    Patient(
        id = "YJ-555 0102",
        name = "Bob Smith",
        age = 62,
        gender = "Male",
        phoneNumber = "+1 555 0102",
        address = "456 Oak Ave, Los Angeles, CA 90001",
        diseases = mutableListOf(
            Disease(
                name = "Arthritis",
                status = "Active",
                severity = "Low",
                diagnosisDate = Date(),
                assignedDoctor = "Dr. Williams",
                notes = "Pain management as needed"
            )
        )
    ),
    Patient(
        id = "YJ-555 0103",
        name = "Carol Williams",
        age = 28,
        gender = "Female",
        phoneNumber = "+1 555 0103",
        address = "789 Pine St, Chicago, IL 60601",
        diseases = mutableListOf()
    )
)
