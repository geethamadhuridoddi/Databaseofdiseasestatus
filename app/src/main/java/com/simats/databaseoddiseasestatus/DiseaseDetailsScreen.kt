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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.LifecycleResumeEffect

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
                isLoadingExtra = false,
                viewModel = viewModel
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

    LifecycleResumeEffect(recordId) {
        if (recordId != null && recordId != -1) {
            diseaseViewModel.fetchDiseaseRecord(recordId, userId = userId)
        }
        patientViewModel.fetchPatients(userId = userId)
        onPauseOrDispose {}
    }

    // Prefer data from the fresh single fetch if available
    val freshRecord = (singleRecordState as? SingleRecordResult.Success)?.record
    
    var resolvedDisease: Disease? = freshRecord?.let { r ->
        Disease(
            recordId = r.recordId ?: recordId,
            name = r.diseaseName,
            status = r.status ?: "Active",
            severity = r.severity ?: "Medium",
            doctorPrimary = r.doctor,
            diagnosisDate = r.diagnosisDate,
            notes = r.notes
        )
    }
    
    var patientId: String? = freshRecord?.patientId?.toString()
    
    if (resolvedDisease == null) {
        for (p in globalPatients) {
            val d = p.diseases?.find { it.recordId == recordId }
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
            val resolvedPatientId = patientId ?: catalogItem?.patientId ?: freshRecord?.patientId
            val patientName = (globalPatients.find { it.id?.toString() == resolvedPatientId }?.name ?: freshRecord?.patientName ?: catalogItem?.patientName ?: "Unknown Patient")

            DiseaseDetailsContent(
                navController = navController,
                paddingValues = paddingValues,
                diseaseName = resolvedDisease.name ?: catalogItem?.displayName ?: "Disease",
                status = resolvedDisease.status,
                patientName = patientName,
                severity = resolvedDisease.severity,
                doctor = resolvedDisease.assignedDoctor,
                diagnosisDate = resolvedDisease.diagnosisDate ?: catalogItem?.diagnosisDate,
                notes = (resolvedDisease.notes ?: catalogItem?.notes)?.takeIf { it.isNotBlank() },
                patientId = resolvedPatientId,
                diseaseId = resolvedDisease.localId,
                recordId = recordId,
                userId = userId,
                isLoadingExtra = isLoading && freshRecord == null,
                viewModel = diseaseViewModel
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                when (singleRecordState) {
                    is SingleRecordResult.Loading -> CircularProgressIndicator()
                    is SingleRecordResult.Error -> Text((singleRecordState as SingleRecordResult.Error).message)
                    else -> Text("No data available.")
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
    isLoadingExtra: Boolean = false,
    viewModel: DiseaseViewModel
) {
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
                
                Text(
                    text = "Patient: $patientName",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

                InfoRow(label = "Severity", value = severity, icon = null)
                InfoRow(label = "Assigned Doctor", value = doctor, icon = null)
                InfoRow(label = "Diagnosis Date", value = diagnosisDate ?: "N/A", icon = Icons.Default.DateRange)

                if (!notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Clinical Notes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notes, fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (recordId != null) {
                        navController.navigate("update_status_direct/$recordId?userId=$userId")
                    } else if (patientId != null && diseaseId != null) {
                        val encodedPid = Uri.encode(patientId)
                        val encodedDid = Uri.encode(diseaseId)
                        navController.navigate("update_status/$encodedPid/$encodedDid?userId=$userId")
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Status")
            }

            OutlinedButton(
                onClick = {
                    if (recordId != null) {
                        viewModel.deletePatientDisease(recordId, userId = userId)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Record")
            }
        }
        
        if (isLoadingExtra) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
