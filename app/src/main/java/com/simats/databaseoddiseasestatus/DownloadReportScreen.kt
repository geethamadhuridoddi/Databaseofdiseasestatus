package com.simats.databaseoddiseasestatus

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadReportScreen(navController: NavController) {
    var selectedFormat by remember { mutableStateOf("pdf") }
    val context = LocalContext.current

    // Register receiver to know when download is done
    DisposableEffect(Unit) {
        val onDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != -1L) {
                    Toast.makeText(context, "Download Completed! Check your Downloads folder.", Toast.LENGTH_LONG).show()
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onDownloadComplete, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(onDownloadComplete, filter)
        }

        onDispose {
            context.unregisterReceiver(onDownloadComplete)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Report", fontWeight = FontWeight.Bold) },
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
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Format", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    FormatSelector(text = "PDF", selected = selectedFormat == "pdf") { selectedFormat = "pdf" }
                    FormatSelector(text = "Excel", selected = selectedFormat == "excel") { selectedFormat = "excel" }
                    FormatSelector(text = "CSV", selected = selectedFormat == "csv") { selectedFormat = "csv" }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    downloadFile(context, selectedFormat)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download ${selectedFormat.uppercase()}")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Note: If you are using an emulator, ensure the server is running on your host machine. If you are using a real device, ensure it's on the same network as the server.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

private fun downloadFile(context: Context, format: String) {
    // Using the IP from ApiClient. If on emulator, you might need to change it to 10.0.2.2 in ApiClient.kt
    val url = "${ApiClient.BASE_URL}reports/download/?format=$format"
    
    Log.d("DownloadReport", "Attempting download from: $url")
    
    val extension = when (format) {
        "excel" -> "xlsx"
        else -> format
    }
    val fileName = "patient_report_${System.currentTimeMillis()}.$extension"

    try {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setTitle("Report: $fileName")
            .setDescription("Downloading patient report from server...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        Toast.makeText(context, "Download started. Check notifications.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("DownloadReport", "Download failed to enqueue", e)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun FormatSelector(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp)
    ) {
        Text(text, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadReportScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DownloadReportScreen(rememberNavController())
    }
}
