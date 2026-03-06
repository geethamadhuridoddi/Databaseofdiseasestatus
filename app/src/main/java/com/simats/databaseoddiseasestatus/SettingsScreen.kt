package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = viewModel()) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedTheme by themeState
    val serverSettings by viewModel.settings
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchSettings()
    }

    // Sync local state with server settings when they arrive
    LaunchedEffect(serverSettings) {
        serverSettings?.let {
            selectedTheme = when (it.theme.lowercase()) {
                "light" -> Theme.Light
                "dark" -> Theme.Dark
                else -> Theme.System
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
        if (isLoading && serverSettings == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Notifications")
                            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Theme")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedTheme == Theme.Light,
                                onClick = { selectedTheme = Theme.Light },
                                label = { Text("Light") }
                            )
                            FilterChip(
                                selected = selectedTheme == Theme.Dark,
                                onClick = { selectedTheme = Theme.Dark },
                                label = { Text("Dark") }
                            )
                            FilterChip(
                                selected = selectedTheme == Theme.System,
                                onClick = { selectedTheme = Theme.System },
                                label = { Text("System") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Language")
                        OutlinedTextField(
                            value = serverSettings?.language ?: "English",
                            onValueChange = { /* Handle language change if needed */ },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { navController.navigate("activity_log") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Activity Log")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val themeStr = when (selectedTheme) {
                            Theme.Light -> "light"
                            Theme.Dark -> "dark"
                            Theme.System -> "system"
                        }
                        viewModel.saveSettings(themeStr, serverSettings?.language ?: "english") { success ->
                            if (success) {
                                Toast.makeText(context, "Settings saved to server", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Failed to save settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Save Settings")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        SettingsScreen(navController = rememberNavController())
    }
}
