package com.simats.databaseoddiseasestatus

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetailsScreen(
    navController: NavController, 
    patientId: String?, 
    diseaseId: String?,
    userId: Int = -1,
    viewModel: DiseaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    patientViewModel: PatientViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val deleteState by viewModel.deleteDiseaseState.collectAsState()
    val singlePatientState by patientViewModel.singlePatientState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteDiseaseResult.Success) {
            Toast.makeText(context, "Disease record deleted successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetDeleteDiseaseState()
            navController.popBackStack()
        } else if (deleteState is DeleteDiseaseResult.Error) {
            Toast.makeText(context, (deleteState as DeleteDiseaseResult.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteDiseaseState()
        }
    }

    val decodedPatientId = remember(patientId) { Uri.decode(patientId ?: "") }
    val decodedDiseaseId = remember(diseaseId) { Uri.decode(diseaseId ?: "") }

    LifecycleResumeEffect(decodedPatientId) {
        if (decodedPatientId.isNotBlank()) {
            patientViewModel.fetchPatientDetails(decodedPatientId, userId = userId)
        }
        onPauseOrDispose {}
    }

    // Prefer fresh data from singlePatientState
    val apiPatient = (singlePatientState as? SinglePatientResult.Success)?.patient
    val patient = apiPatient ?: globalPatients.find { it.id?.toString() == decodedPatientId }
    val disease = patient?.diseases?.find { it.localId == decodedDiseaseId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Details", fontWeight = FontWeight.Bold) },
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
        if (patient != null && disease != null) {
            DiseaseDetailsContent(
                navController = navController,
                paddingValues = paddingValues,
                diseaseName = disease.name ?: "Unknown",
                status = disease.status,
                patientName = patient.name ?: "Unknown",
                severity = disease.severity,
                doctor = disease.assignedDoctor,
                diagnosisDate = disease.diagnosisDate,
                notes = disease.notes,
                patientId = patient.id?.toString(),
                diseaseId = disease.localId,
                recordId = disease.recordId,
                userId = userId,
                isLoadingExtra = false
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Disease record not found.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetailsByRecordScreen(
    navController: NavController,
    recordId: Int?,
    userId: Int = -1,
    patientViewModel: PatientViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    diseaseViewModel: DiseaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val deleteState by diseaseViewModel.deleteDiseaseState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteDiseaseResult.Success) {
            Toast.makeText(context, "Disease record deleted successfully", Toast.LENGTH_SHORT).show()
            diseaseViewModel.resetDeleteDiseaseState()
            navController.popBackStack()
        } else if (deleteState is DeleteDiseaseResult.Error) {
            Toast.makeText(context, (deleteState as DeleteDiseaseResult.Error).message, Toast.LENGTH_LONG).show()
            diseaseViewModel.resetDeleteDiseaseState()
        }
    }

    val catalogItem = selectedDiseaseCatalogItem
    val patientsState by patientViewModel.patientsState.collectAsState()
    val singleRecordState by diseaseViewModel.singleRecordState.collectAsState()
    
    val targetRecordId = recordId

    LaunchedEffect(targetRecordId) {
        if (targetRecordId != null && targetRecordId != -1) {
            diseaseViewModel.fetchDiseaseRecord(targetRecordId, userId = userId)
        }
        patientViewModel.fetchPatients(userId = userId)
    }

    // Prefer data from the fresh single fetch if available
    val freshRecord = (singleRecordState as? SingleRecordResult.Success)?.record
    
    var resolvedDisease: Disease? = freshRecord?.let { r ->
        Disease(
            recordId = r.recordId ?: targetRecordId,
            name = r.diseaseName,
            status = r.status ?: "Active",
            severity = r.severity ?: "Medium",
            doctorPrimary = r.doctor,
            diagnosisDate = r.date,
            notes = r.notes
        )
    }
    
    var patientId: String? = freshRecord?.patientId?.toString()
    
    if (resolvedDisease == null) {
        for (p in globalPatients) {
            val d = p.diseases?.find { it.recordId == targetRecordId }
            if (d != null) {
                resolvedDisease = d
                patientId = p.id?.toString()
                break
            }
        }
    }

    val isLoading = patientsState is PatientsResult.Loading || singleRecordState is SingleRecordResult.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Details", fontWeight = FontWeight.Bold) },
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
        if (resolvedDisease != null) {
            val resolvedPatientId = patientId ?: catalogItem?.patientId ?: freshRecord?.patientId?.toString()
            val patientName = (globalPatients.find { it.id?.toString() == resolvedPatientId }?.name ?: freshRecord?.patientName ?: catalogItem?.patientName ?: "Unknown Patient")

            DiseaseDetailsContent(
                navController = navController,
                paddingValues = paddingValues,
                diseaseName = resolvedDisease?.name ?: catalogItem?.displayName ?: "Disease",
                status = resolvedDisease?.status ?: catalogItem?.status ?: "Active",
                patientName = patientName,
                severity = resolvedDisease?.severity ?: catalogItem?.severity ?: "Medium",
                doctor = resolvedDisease?.assignedDoctor ?: catalogItem?.doctor ?: "Not Assigned",
                diagnosisDate = resolvedDisease?.diagnosisDate ?: catalogItem?.diagnosisDate,
                notes = (resolvedDisease?.notes ?: catalogItem?.notes)?.takeIf { it.isNotBlank() },
                patientId = resolvedPatientId,
                diseaseId = resolvedDisease?.localId,
                recordId = targetRecordId,
                userId = userId,
                isLoadingExtra = isLoading && resolvedDisease == null
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (singleRecordState is SingleRecordResult.Loading) {
                    CircularProgressIndicator()
                } else if (singleRecordState is SingleRecordResult.Error) {
                    Text((singleRecordState as SingleRecordResult.Error).message)
                } else {
                    Text("No data available.")
                }
            }
        }
    }
}

@Composable
fun DiseaseDetailsContent(
    navController: NavController,
    paddingValues: PaddingValues,
    diseaseName: String,
    status: String,
    patientName: String,
    severity: String,
    doctor: String,
    diagnosisDate: String?,
    notes: String?,
    patientId: String?,
    diseaseId: String?,
    recordId: Int? = null,
    userId: Int = -1,
    isLoadingExtra: Boolean = false
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        diseaseName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val (badgeBg, badgeText) = when (status.lowercase()) {
                        "active"     -> Color(0xFFFFF9C4) to Color(0xFFF57F17)
                        "recovered"  -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        "recovering" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
                        "critical"   -> Color(0xFFFFEBEE) to Color(0xFFC62828)
                        else         -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
                    }
                    Surface(color = badgeBg, shape = MaterialTheme.shapes.small) {
                        Text(
                            text = status.replaceFirstChar { it.uppercase() },
                            color = badgeText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(patientName, fontSize = 15.sp, color = Color.DarkGray)
                // Show phone number if we have the patient record
                globalPatients.find { it.id?.toString() == patientId }?.displayPhone?.takeIf { it.isNotBlank() }?.let {
                    Text("Phone: $it", fontSize = 13.sp, color = Color.Gray)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFE0E0E0))

                DetailRow(label = "Severity", value = severity)
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(label = "Assigned Doctor", value = doctor.takeIf { it.isNotBlank() } ?: "Not assigned")
                
                // Date removed per request

                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(label = "Notes", value = notes?.takeIf { it.isNotBlank() } ?: "None")

                if (isLoadingExtra) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading details…", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (recordId != null) {
                        navController.navigate("update_status_direct/$recordId?userId=$userId")
                    } else if (patientId != null && diseaseId != null) {
                        val encodedPid = Uri.encode(patientId)
                        val encodedDid = Uri.encode(diseaseId)
                        navController.navigate("update_status/$encodedPid/$encodedDid?recordId=$recordId&userId=$userId")
                    } else {
                        Toast.makeText(context, "Cannot update: ID mapping missing.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Status", fontSize = 12.sp)
            }

            var showDeleteDialog by remember { mutableStateOf(false) }
            val diseaseViewModel: DiseaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Record", fontSize = 12.sp)
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Disease Record") },
                    text = { Text("Are you sure you want to delete this disease record? This cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                recordId?.let { diseaseViewModel.deletePatientDisease(it, userId = userId) }
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

            OutlinedButton(
                onClick = {
                    if (patientId != null) {
                        val encodedPid = Uri.encode(patientId)
                        navController.navigate("disease_history/$encodedPid?diseaseName=$diseaseName&userId=$userId")
                    } else {
                        Toast.makeText(context, "Patient history unavailable.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View History", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(text = value, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Preview(showBackground = true)
@Composable
fun DiseaseDetailsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DiseaseDetailsScreen(navController = rememberNavController(), patientId = "YJ-555 0101", diseaseId = null)
    }
}
