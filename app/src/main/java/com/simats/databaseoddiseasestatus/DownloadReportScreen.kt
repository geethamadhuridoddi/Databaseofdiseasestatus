package com.simats.databaseoddiseasestatus

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadReportScreen(navController: NavController, userId: Int = -1) {
    var selectedFormat by remember { mutableStateOf("pdf") }
    val context = LocalContext.current

    // Register receiver to know when download is done
    DisposableEffect(Unit) {
        val onDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                if (id != -1L && context != null) {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query().setFilterById(id)
                    val cursor: Cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(statusIndex)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(context, "Download Completed! Check your Downloads folder.", Toast.LENGTH_LONG).show()
                        } else {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = cursor.getInt(reasonIndex)
                            Log.e("DownloadReport", "Download failed with status $status, reason: $reason")
                            Toast.makeText(context, "Download Failed. Reason: $reason", Toast.LENGTH_LONG).show()
                        }
                    }
                    cursor.close()
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onDownloadComplete, filter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                onDownloadComplete,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
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
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Export Format", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FormatSelector("PDF Document (.pdf)", selectedFormat == "pdf") { selectedFormat = "pdf" }
                    FormatSelector("Excel Spreadsheet (.xlsx)", selectedFormat == "excel") { selectedFormat = "excel" }
                    FormatSelector("CSV File (.csv)", selectedFormat == "csv") { selectedFormat = "csv" }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { downloadFile(context, selectedFormat, userId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Download ${selectedFormat.uppercase()}")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "The report will be saved to your device's Downloads folder.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

private fun downloadFile(context: Context, format: String, userId: Int = -1) {
    val url = if (userId != -1) {
        "${ApiClient.BASE_URL}reports/download/?format=$format&user_id=$userId"
    } else {
        "${ApiClient.BASE_URL}reports/download/?format=$format"
    }
    
    val (extension, mimeType) = when (format) {
        "excel" -> "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "csv" -> "csv" to "text/csv"
        else -> "pdf" to "application/pdf"
    }
    
    val fileName = "Patient_Report_${System.currentTimeMillis()}.$extension"

    try {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setTitle(fileName)
            .setDescription("Downloading medical report...")
            .setMimeType(mimeType)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("DownloadReport", "Download failed", e)
        Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun FormatSelector(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadReportScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DownloadReportScreen(rememberNavController())
    }
}
