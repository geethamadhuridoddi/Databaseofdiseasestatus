package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(navController: NavController, patientId: String?) {
    val patient = globalPatients.find { it.id == patientId }

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
                                Text(patient.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { navController.navigate("edit_patient/${patient.id}") }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                            }
                            Text("${patient.age} years • ${patient.gender}")
                            Text("Phone: ${patient.phoneNumber}")
                            Text("Address: ${patient.address}")
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
                        Button(onClick = { navController.navigate("add_disease/${patient.id}") }) {
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
                            .clickable { navController.navigate("disease_details/${patient.id}/${disease.id}") },
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
                            Column {
                                Text(disease.name, fontWeight = FontWeight.Bold)
                                Text(disease.severity)
                            }
                            Text(disease.status)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { navController.navigate("disease_history/${patient.id}") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View History")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Patient not found.")
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
