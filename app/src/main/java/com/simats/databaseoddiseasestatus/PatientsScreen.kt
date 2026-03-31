package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    navController: NavController, 
    doctorName: String? = null,
    initialFilter: String? = null,
    userId: Int = -1,
    viewModel: PatientViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf<String?>("All") }
    var currentStatusFilter by remember(initialFilter) { mutableStateOf(initialFilter ?: "All") }
    val context = LocalContext.current

    val patientsState by viewModel.patientsState.collectAsState()
    val deleteState by viewModel.deletePatientState.collectAsState()
    var patientToDelete by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is DeletePatientResult.Success -> {
                Toast.makeText(context, "Patient deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetDeletePatientState()
            }
            is DeletePatientResult.Error -> {
                Toast.makeText(context, (deleteState as DeletePatientResult.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetDeletePatientState()
            }
            else -> {}
        }
    }

    LifecycleResumeEffect(currentStatusFilter) {
        try {
            val apiFilter = when (currentStatusFilter) {
                "All" -> null
                "HasDisease" -> null
                else -> currentStatusFilter
            }
            android.util.Log.d("PatientsScreen", "Fetching patients: filter=$apiFilter, userId=$userId")
            viewModel.fetchPatients(apiFilter, userId = if (userId != -1) userId else null)
        } catch (e: Exception) {
            android.util.Log.e("PatientsScreen", "Error fetching patients", e)
        }
        onPauseOrDispose {}
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA) 
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A65FF)) 
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Patients", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Normal)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search patients...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color(0xFF1A65FF)
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                val items = listOf("All", "Male", "Female")

                Box {
                    Surface(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFD0D0D0)),
                        color = Color.Transparent, 
                        modifier = Modifier.width(90.dp).height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedSex == "All") "Sex" else (selectedSex ?: "Sex"),
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedSex = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { 
                        val route = if (!doctorName.isNullOrBlank()) {
                            "add_patient?doctorName=${android.net.Uri.encode(doctorName)}&userId=$userId"
                        } else {
                            "add_patient?userId=$userId"
                        }
                        navController.navigate(route) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A65FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Patient", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF1A65FF))
                )
                
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    val currentPatients = when (val state = patientsState) {
                        is PatientsResult.Success -> state.patients
                        else -> emptyList()
                    }

                    if (patientsState is PatientsResult.Error) {
                        Text(
                            text = "Error: ${(patientsState as PatientsResult.Error).message}",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (patientsState is PatientsResult.Loading && currentPatients.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1A65FF))
                    } else {
                        val filterVal = currentStatusFilter ?: "All"
                        val filteredPatients = currentPatients.filter { patient ->
                            val nameMatches = patient.name?.contains(searchQuery, ignoreCase = true) ?: searchQuery.isEmpty()
                            val sexMatches = selectedSex == "All" || patient.gender?.equals(selectedSex, ignoreCase = true) == true
                            
                            val statusMatches = when (filterVal) {
                                "All" -> true
                                "HasDisease" -> (patient.diseaseCount ?: 0) > 0 || !patient.diseases.isNullOrEmpty()
                                else -> {
                                    // If we fetched the patient using a status filter from the API, 
                                    // we can trust that the API returned patients who match that status.
                                    // We only double-filter if we are in "All" or "HasDisease" view where we want to filter from the full list.
                                    // However, to keep the client-side interactivity consistent:
                                    patient.diseases?.any { d ->
                                        d.status.trim().equals(filterVal, ignoreCase = true) || 
                                        d.severity.trim().equals(filterVal, ignoreCase = true)
                                    } ?: true // Fallback to true if we don't have disease info yet
                                }
                            }

                            nameMatches && sexMatches && statusMatches
                        }

                        if (filteredPatients.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "No patients found", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(filteredPatients, key = { it.id ?: it.hashCode() }) { patient ->
                                    PatientListItem(
                                        patient = patient,
                                        onClick = {
                                            val idToUse = patient.id?.toString()
                                            if (!idToUse.isNullOrBlank()) {
                                                val encodedDoctor = android.net.Uri.encode(doctorName ?: "")
                                                navController.navigate("patient_details/$idToUse?doctorName=$encodedDoctor&userId=$userId")
                                            } else {
                                                Toast.makeText(context, "Patient ID not found", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onDelete = {
                                            patientToDelete = patient
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } 
        
        if (patientToDelete != null) {
            AlertDialog(
                onDismissRequest = { patientToDelete = null },
                title = { Text("Delete Patient") },
                text = { Text("Are you sure you want to delete ${patientToDelete?.name}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            patientToDelete?.id?.let { viewModel.deletePatient(it.toString(), userId = if (userId != -1) userId else null) }
                            patientToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { patientToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PatientListItem(patient: Patient, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = patient.name ?: "Unknown",
                    fontSize = 18.sp,
                    color = Color(0xFF1E1E1E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray.copy(alpha = 0.5f)) 
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val diseaseCount = patient.diseaseCount ?: patient.diseases?.size ?: 0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.width(100.dp)) {
                    Text(
                        text = "${patient.age ?: 0}",
                        color = Color(0xFF666666),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "years",
                        color = Color(0xFF666666),
                        fontSize = 15.sp,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = patient.displayPhone.takeIf { it.isNotBlank() } ?: "", 
                        color = Color(0xFF9E9E9E), 
                        fontSize = 15.sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = Color(0xFF666666), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "${patient.gender ?: "Unknown"}",
                        color = Color(0xFF666666),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text("•", color = Color(0xFF666666), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column {
                        Text(
                            text = "$diseaseCount",
                            color = Color(0xFF666666),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "diseases",
                            color = Color(0xFF666666),
                            fontSize = 15.sp,
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatientsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        PatientsScreen(navController = rememberNavController())
    }
}
