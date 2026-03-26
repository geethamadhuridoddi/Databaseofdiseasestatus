package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateStatusScreen(
    navController: NavController, 
    patientId: String?, 
    diseaseId: String?,
    recordId: Int? = null,
    userId: Int = -1,
    viewModel: DiseaseViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel()
) {
    val context = LocalContext.current
    val patientsState by patientViewModel.patientsState.collectAsState()
    val singlePatientState by patientViewModel.singlePatientState.collectAsState()

    val d_pid = remember(patientId) { android.net.Uri.decode(patientId ?: "") }
    val d_did = remember(diseaseId) { android.net.Uri.decode(diseaseId ?: "") }
    val rid = recordId

    LaunchedEffect(rid, patientsState) {
        if (rid != null && rid != -1 && globalPatients.none { p -> p.diseases?.any { it.recordId == rid } == true }) {
            val catalogPid = if (selectedDiseaseCatalogItem?.recordId == rid) selectedDiseaseCatalogItem?.patientId else null
            val effectivePid = d_pid.takeIf { it.isNotBlank() } ?: catalogPid
            
            if (!effectivePid.isNullOrBlank()) {
                patientViewModel.fetchPatientDetails(effectivePid, userId = if (userId != -1) userId else null)
            }
        }
    }

    val lookupResult = remember(patientsState, singlePatientState, d_pid, d_did, rid, globalPatients.toList()) {
        if (rid != null && rid != -1) {
            for (p in globalPatients) {
                val d = p.diseases?.find { it.recordId == rid }
                if (d != null) return@remember Pair(p, d)
            }
        }
        if (d_pid.isNotBlank() && d_did.isNotBlank()) {
            val p = globalPatients.find { it.id.toString() == d_pid } ?: 
                    (singlePatientState as? SinglePatientResult.Success)?.patient?.takeIf { it.id.toString() == d_pid }
            val d = p?.diseases?.find { it.localId == d_did }
            if (d != null) return@remember Pair(p, d)
        }
        for (p in globalPatients) {
            val d = p.diseases?.find { it.localId == d_did || (rid != null && it.recordId == rid) }
            if (d != null) return@remember Pair(p, d)
        }
        if (rid != null && selectedDiseaseCatalogItem?.recordId == rid) {
            val catalog = selectedDiseaseCatalogItem!!
            val d = Disease(
                recordId = catalog.recordId,
                name = catalog.displayName,
                status = catalog.status ?: "Active",
                severity = catalog.severity ?: "Medium",
                doctorPrimary = catalog.doctor,
                diagnosisDate = catalog.diagnosisDate,
                notes = catalog.notes,
                providedLocalId = d_did.takeIf { it.isNotBlank() }
            ).apply {
                explicitDoctor = catalog.doctor
            }
            val p = globalPatients.find { it.id.toString() == catalog.patientId } ?: Patient(
                id = catalog.patientId?.toIntOrNull() ?: 0,
                name = catalog.patientName ?: "Unknown",
                age = 0,
                gender = "Unknown",
                phone = "",
                address = "",
                disease_count = 0
            )
            return@remember Pair(p, d)
        }
        null
    }

    val foundPatient = lookupResult?.first
    val foundDisease = lookupResult?.second

    if (foundDisease == null) {
        if (patientsState is PatientsResult.Loading || singlePatientState is SinglePatientResult.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3F51B5))
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Content Not Found", fontWeight = FontWeight.Bold)
                    Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
            }
        }
        return
    }

    var status by remember(foundDisease) { mutableStateOf(foundDisease.status) }
    var notes by remember(foundDisease) { mutableStateOf(foundDisease.notes ?: "") }
    var assignedDoctor by remember(foundDisease) { mutableStateOf(foundDisease.assignedDoctor) }
    var severity by remember(foundDisease) { mutableStateOf(foundDisease.severity) }
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Active", "Recovering", "Recovered", "Critical")
    val updateState by viewModel.updateDiseaseState.collectAsState()

    LaunchedEffect(updateState) {
        if (updateState is UpdateDiseaseResult.Success) {
            // Update globalPatients for immediate local sync
            val targetPid = foundPatient?.id.toString()
            val targetRid = foundDisease.recordId ?: rid
            if (targetPid.isNotBlank() && targetRid != null) {
                val pIdx = globalPatients.indexOfFirst { it.id.toString() == targetPid }
                if (pIdx != -1) {
                    val p = globalPatients[pIdx]
                    val dList = p.diseases?.toMutableList() ?: mutableListOf()
                    val dIdx = dList.indexOfFirst { it.recordId == targetRid }
                    if (dIdx != -1) {
                        dList[dIdx] = dList[dIdx].copy(
                            status = status, 
                            notes = notes,
                            doctorPrimary = assignedDoctor,
                            severity = severity
                        )
                        globalPatients[pIdx] = p.copy(diseases = dList)
                    }
                }
            }

            if (selectedDiseaseCatalogItem?.recordId == targetRid) {
                selectedDiseaseCatalogItem = selectedDiseaseCatalogItem?.copy(
                    status = status, 
                    notes = notes,
                    doctor = assignedDoctor,
                    severity = severity
                )
            }
            
            Toast.makeText(context, "Status updated successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.resetUpdateDiseaseState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {
            Text(text = foundDisease.name ?: "Unknown", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = "Patient: ${foundPatient?.name ?: "Unknown"}", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text("Assigned Doctor: ", fontWeight = FontWeight.SemiBold)
                Text(foundDisease.assignedDoctor.takeIf { it.isNotBlank() } ?: "Not assigned")
            }

            Spacer(modifier = Modifier.height(24.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = status, onValueChange = {}, readOnly = true, label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { status = option; expanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = assignedDoctor, onValueChange = { assignedDoctor = it }, label = { Text("Assigned Doctor") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = notes, onValueChange = { notes = it }, label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            if (updateState is UpdateDiseaseResult.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val finalRid = foundDisease.recordId ?: rid
                        if (finalRid != null && finalRid != -1) {
                            val updateData = mutableMapOf<String, Any>(
                                "status" to status, 
                                "notes" to notes,
                                "severity" to severity,
                                "assigned_doctor" to assignedDoctor
                            )
                            if (userId != -1) {
                                updateData["user_id"] = userId
                            }
                            viewModel.updatePatientDiseaseStatus(finalRid, updateData)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56FF))
                ) {
                    Text("Save Update", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
