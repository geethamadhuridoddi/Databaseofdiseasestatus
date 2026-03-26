package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

// Global holder for the currently selected disease catalog item
var selectedDiseaseCatalogItem: DiseaseCatalogItem? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseasesScreen(
    navController: NavController, 
    userId: Int = -1,
    diseaseViewModel: DiseaseViewModel = viewModel(),
    patientViewModel: PatientViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf<String?>(null) } // null for All

    val diseasesState by diseaseViewModel.diseasesState.collectAsState()

    // Fetch diseases and patients from DB
    LaunchedEffect(userId) {
        diseaseViewModel.fetchDiseases(userId = if (userId != -1) userId else null)
        patientViewModel.fetchPatients(userId = if (userId != -1) userId else null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diseases", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search diseases...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(selected = selectedSeverity == null, onClick = { selectedSeverity = null }, label = { Text("All") })
                FilterChip(selected = selectedSeverity == "Low", onClick = { selectedSeverity = "Low" }, label = { Text("Low") })
                FilterChip(selected = selectedSeverity == "Medium", onClick = { selectedSeverity = "Medium" }, label = { Text("Medium") })
                FilterChip(selected = selectedSeverity == "High", onClick = { selectedSeverity = "High" }, label = { Text("High") })
                FilterChip(selected = selectedSeverity == "Critical", onClick = { selectedSeverity = "Critical" }, label = { Text("Critical") })
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = diseasesState) {
                    is DiseasesResult.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is DiseasesResult.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    is DiseasesResult.Success -> {
                        val filteredData = state.diseases.filter { disease ->
                            val nameMatches = disease.displayName.contains(searchQuery, ignoreCase = true) || 
                                             (disease.patientName ?: "").contains(searchQuery, ignoreCase = true)
                            val severityMatches = selectedSeverity == null || 
                                                 (disease.severity ?: "").equals(selectedSeverity, ignoreCase = true)
                            nameMatches && severityMatches
                        }
                        
                        if (filteredData.isEmpty()) {
                            Text("No diseases found.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 8.dp)
                            ) {
                                items(filteredData) { diseaseItem ->
                                    DiseaseCatalogListItem(
                                        diseaseItem = diseaseItem,
                                        onClick = {
                                            val rid = diseaseItem.recordId
                                            if (rid != null) {
                                                selectedDiseaseCatalogItem = diseaseItem
                                                navController.navigate("disease_details_record/$rid?userId=$userId")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun DiseaseCatalogListItem(diseaseItem: DiseaseCatalogItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(diseaseItem.displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(diseaseItem.status ?: "Unknown Status", style = MaterialTheme.typography.bodySmall, color = Color(0xFF3F51B5))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Patient: ${diseaseItem.patientName ?: "Unknown"}", fontSize = 16.sp)
            Text("Doctor: ${diseaseItem.doctor ?: "Not Assigned"}", fontSize = 14.sp)
            Text("Severity: ${diseaseItem.severity ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiseasesScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DiseasesScreen(navController = rememberNavController())
    }
}
