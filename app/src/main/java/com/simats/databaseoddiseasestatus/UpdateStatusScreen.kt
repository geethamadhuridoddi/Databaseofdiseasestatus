package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateStatusScreen(navController: NavController, patientId: String?, diseaseId: String?) {
    val patient = globalPatients.find { it.id == patientId }
    val disease = patient?.diseases?.find { it.id == diseaseId }

    if (patient == null || disease == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Disease not found.")
        }
        return
    }

    var status by remember { mutableStateOf(disease.status) }
    var remarks by remember { mutableStateOf(disease.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Status", fontWeight = FontWeight.Bold) },
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
            Text(disease.name, fontWeight = FontWeight.Bold)
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
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val patientIndex = globalPatients.indexOf(patient)
                    val diseaseIndex = patient.diseases?.indexOf(disease) ?: -1
                    if (patientIndex != -1 && diseaseIndex != -1) {
                        val updatedDisease = disease.copy(status = status, notes = remarks)
                        globalPatients[patientIndex].diseases?.set(diseaseIndex, updatedDisease)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text("Save Update", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateStatusScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        val firstPatient = globalPatients.firstOrNull()
        val firstDiseaseId = firstPatient?.diseases?.firstOrNull()?.id ?: ""
        UpdateStatusScreen(navController = rememberNavController(), patientId = firstPatient?.id, diseaseId = firstDiseaseId)
    }
}
