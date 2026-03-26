package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController, userId: Int = -1, viewModel: ReportViewModel = viewModel()) {
    val summary by viewModel.summary
    val dashboardStats by viewModel.dashboardStats
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.fetchReportData(if (userId != -1) userId else null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    val totalCases = dashboardStats?.totalCases ?: summary?.totalCases ?: 0
                    val recoveryRate = dashboardStats?.let {
                        if (it.totalCases > 0) {
                            (it.recoveringCases.toDouble() / it.totalCases * 100)
                        } else 0.0
                    } ?: summary?.recoveryRate ?: 0.0

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReportStatCard(
                            title = "Total Cases",
                            value = totalCases.toString(),
                            icon = Icons.Default.BarChart,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            title = "Recovery Rate",
                            value = String.format(Locale.US, "%.1f%%", recoveryRate),
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text(
                        text = "Report Types",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }
                item {
                    ReportTypeItem(
                        icon = Icons.Default.Description,
                        title = "Patient Report",
                        description = "Detailed patient data and statistics",
                        onClick = { navController.navigate("patient_report?userId=$userId") }
                    )
                }
                item {
                    ReportTypeItem(
                        icon = Icons.Default.Download,
                        title = "Download Reports",
                        description = "Export data in various formats",
                        onClick = { navController.navigate("download_report?userId=$userId") }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportStatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(title, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTypeItem(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        ReportsScreen(rememberNavController())
    }
}
