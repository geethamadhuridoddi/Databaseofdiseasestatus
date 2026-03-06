package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetailsScreen(navController: NavController, patientId: String?, diseaseId: String?) {
    val patient = globalPatients.find { it.id == patientId }
    val disease = patient?.diseases?.find { it.id == diseaseId }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
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
                            Text(disease.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(disease.status, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Text(patient.name)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Diagnosis Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(disease.diagnosisDate)}")
                        Text("Severity: ${disease.severity}")
                        Text("Assigned Doctor: ${disease.assignedDoctor}")
                        disease.notes?.let { Text("Notes: $it") }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = { navController.navigate("update_status/${patient.id}/${disease.id}") }, modifier = Modifier.weight(1f)) {
                        Text("Update Status")
                    }
                    OutlinedButton(onClick = { navController.navigate("disease_history/${patient.id}") }, modifier = Modifier.weight(1f)) {
                        Text("View History")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Disease not found.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiseaseDetailsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DiseaseDetailsScreen(navController = rememberNavController(), patientId = "YJ-555 0101", diseaseId = globalPatients.first().diseases?.first()?.id)
    }
}
