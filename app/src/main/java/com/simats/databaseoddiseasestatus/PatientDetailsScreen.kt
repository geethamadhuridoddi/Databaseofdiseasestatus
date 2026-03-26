package com.simats.databaseoddiseasestatus

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    navController: NavController, 
    patientId: String?,
    doctorName: String? = null,
    userId: Int = -1,
    viewModel: PatientViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val localContext = LocalContext.current
    val deleteState by viewModel.deletePatientState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is DeletePatientResult.Success -> {
                android.widget.Toast.makeText(localContext, "Patient deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.resetDeletePatientState()
                navController.popBackStack()
            }
            is DeletePatientResult.Error -> {
                android.widget.Toast.makeText(localContext, (deleteState as DeletePatientResult.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetDeletePatientState()
            }
            else -> {}
        }
    }
    val singlePatientState by viewModel.singlePatientState.collectAsState()
    val decodedPatientId = remember(patientId) { android.net.Uri.decode(patientId ?: "") }

    LifecycleResumeEffect(decodedPatientId) {
        if (decodedPatientId.isNotBlank()) {
            viewModel.resetSinglePatientState()
            viewModel.fetchPatientDetails(decodedPatientId, userId = if (userId != -1) userId else null)
        }
        onPauseOrDispose {}
    }
    // Always prefer the fresh API result so diseases list is up-to-date
    val apiPatient = (singlePatientState as? SinglePatientResult.Success)?.patient
    val patient = apiPatient ?: globalPatients.find { it.id?.toString() == decodedPatientId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Details", fontWeight = FontWeight.Bold) },
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
        if (patient != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(patient.name ?: "Unknown", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Row {
                                    IconButton(onClick = { navController.navigate("edit_patient/${patient.id?.toString()}?userId=$userId") }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                            Text("${patient.age ?: 0} years • ${patient.gender ?: "Unknown"}")
                            Text("Phone: ${patient.displayPhone.takeIf { it.isNotBlank() } ?: "N/A"}")
                            Text("Address: ${patient.address ?: "N/A"}")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Disease Records", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Button(onClick = { 
                            val encodedDoctor = android.net.Uri.encode(doctorName ?: "")
                            navController.navigate("add_disease/${patient.id?.toString()}?doctorName=$encodedDoctor&userId=$userId")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Disease")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }

                items(patient.diseases ?: emptyList()) { disease ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                val encodedPid = android.net.Uri.encode(patient.id?.toString() ?: "")
                                val encodedDid = android.net.Uri.encode(disease.localId ?: "")
                                navController.navigate("disease_details/$encodedPid/$encodedDid?userId=$userId") 
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(disease.name ?: "Unknown", fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(disease.severity, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(" • ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(disease.status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF3F51B5))
                                }
                                if (disease.assignedDoctor.isNotBlank()) {
                                    Text(
                                        text = "Doctor: ${disease.assignedDoctor}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            
                            IconButton(onClick = {
                                val encodedPid = android.net.Uri.encode(patient.id?.toString() ?: "")
                                val encodedDid = android.net.Uri.encode(disease.localId ?: "")
                                val rid = disease.recordId
                                val query = if (rid != null) "?recordId=$rid&userId=$userId" else "?userId=$userId"
                                Log.i("API_DEBUG", "Navigating to Update from PatientDetails: rid=$rid")
                                navController.navigate("update_status/$encodedPid/$encodedDid$query")
                            }) {
                                Icon(
                                    Icons.Default.Edit, 
                                    contentDescription = "Update Status",
                                    tint = Color(0xFF3F51B5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("disease_history/${patient.id?.toString()}?userId=$userId") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View History")
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Patient") },
                    text = { Text("Are you sure you want to delete this patient (and all their disease records)? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                patient.id?.let { viewModel.deletePatient(it.toString(), userId = if (userId != -1) userId else null) }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (singlePatientState) {
                    is SinglePatientResult.Loading -> CircularProgressIndicator(color = Color(0xFF3F51B5))
                    is SinglePatientResult.Error -> Text((singlePatientState as SinglePatientResult.Error).message ?: "Unknown patient error")
                    else -> Text("Patient not found.")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatientDetailsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        PatientDetailsScreen(navController = rememberNavController(), patientId = "YJ-555 0101")
    }
}
