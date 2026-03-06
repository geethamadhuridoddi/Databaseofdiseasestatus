package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientReportScreen(navController: NavController, viewModel: ReportViewModel = viewModel()) {
    val patientReport by viewModel.patientReport
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.fetchPatientReport()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Report", fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Patient Data", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { navController.navigate("download_report") }) {
                    Icon(Icons.Default.Download, contentDescription = "Export")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        item {
                            Row(Modifier.fillMaxWidth().background(Color.LightGray.copy(alpha = 0.2f)).padding(vertical = 8.dp)) {
                                Text("Name", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Start, color = Color.Black)
                                Text("Age", modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Diseases", modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                        items(patientReport) { item ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(item.name, modifier = Modifier.weight(1f))
                                Text(item.age.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                Text(item.diseases.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatientReportScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        PatientReportScreen(rememberNavController())
    }
}
