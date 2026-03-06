package com.simats.databaseoddiseasestatus

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiseaseScreen(
    navController: NavController, 
    patientId: String?, 
    viewModel: DiseaseViewModel = viewModel()
) {
    val patient = globalPatients.find { it.id == patientId }
    val context = LocalContext.current
    val addState by viewModel.addDiseaseState.collectAsState()

    LaunchedEffect(addState) {
        if (addState is AddDiseaseResult.Success) {
            Toast.makeText(context, "Disease added successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetAddDiseaseState()
            navController.popBackStack()
        } else if (addState is AddDiseaseResult.Error) {
            Toast.makeText(context, (addState as AddDiseaseResult.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetAddDiseaseState()
        }
    }

    if (patient == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Patient not found (ID: $patientId)")
        }
        return
    }

    var diseaseName by remember { mutableStateOf("") }
    var diagnosisDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var severity by remember { mutableStateOf("Medium") }
    var status by remember { mutableStateOf("Active") }
    var assignedDoctor by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Disease", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3F51B5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Patient: ${patient.name} (ID: ${patient.id})")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = diseaseName,
                onValueChange = { diseaseName = it },
                label = { Text("Disease Name *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(diagnosisDateMillis)),
                onValueChange = {},
                label = { Text("Diagnosis Date *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker.value = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Severity *", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = severity == "Low", onClick = { severity = "Low" }, label = { Text("Low") })
                FilterChip(selected = severity == "Medium", onClick = { severity = "Medium" }, label = { Text("Medium") })
                FilterChip(selected = severity == "High", onClick = { severity = "High" }, label = { Text("High") })
                FilterChip(selected = severity == "Critical", onClick = { severity = "Critical" }, label = { Text("Critical") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Status *", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = status == "Active", onClick = { status = "Active" }, label = { Text("Active") })
                FilterChip(selected = status == "Recovering", onClick = { status = "Recovering" }, label = { Text("Recovering") })
                FilterChip(selected = status == "Recovered", onClick = { status = "Recovered" }, label = { Text("Recovered") })
                FilterChip(selected = status == "Critical", onClick = { status = "Critical" }, label = { Text("Critical") })
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = assignedDoctor,
                onValueChange = { assignedDoctor = it },
                label = { Text("Assigned Doctor *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            if (addState is AddDiseaseResult.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val trimmedName = diseaseName.trim()
                        val trimmedDoctor = assignedDoctor.trim()

                        if (trimmedName.isBlank() || trimmedDoctor.isBlank() || patientId.isNullOrBlank()) {
                            val missing = mutableListOf<String>()
                            if (trimmedName.isBlank()) missing.add("Disease Name")
                            if (trimmedDoctor.isBlank()) missing.add("Doctor")
                            if (patientId.isNullOrBlank()) missing.add("Patient ID")
                            
                            Toast.makeText(context, "Missing fields: ${missing.joinToString(", ")}", Toast.LENGTH_SHORT).show()
                            Log.e("AddDisease", "Validation failed. Missing: $missing")
                            return@Button
                        }
                        
                        val diseaseData = mapOf(
                            "patient_id" to (patientId.toIntOrNull() ?: patientId),
                            "name" to trimmedName,
                            "status" to status,
                            "severity" to severity,
                            "diagnosis_date" to SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(diagnosisDateMillis)),
                            "assigned_doctor" to trimmedDoctor,
                            "notes" to notes.trim()
                        )
                        viewModel.addDisease(diseaseData)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Save Disease", color = Color.White)
                }
            }
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = diagnosisDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            diagnosisDateMillis = it
                        }
                        showDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker.value = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDiseaseScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        AddDiseaseScreen(navController = rememberNavController(), patientId = "1")
    }
}
