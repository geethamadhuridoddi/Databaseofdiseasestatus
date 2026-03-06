package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, viewModel: NotificationViewModel = viewModel()) {
    val notificationsState by viewModel.notificationsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        globalNotifications.forEach { viewModel.deleteNotification(it.id) }
                    }) {
                        Text("Clear All", color = Color.White)
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = notificationsState) {
                is NotificationsResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NotificationsResult.Success -> {
                    if (state.notifications.isEmpty()) {
                        Text("No notifications", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(state.notifications.reversed()) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onDelete = { viewModel.deleteNotification(notification.id) },
                                    onMarkRead = { if (!notification.isRead) viewModel.markAsRead(notification.id) }
                                )
                            }
                        }
                    }
                }
                is NotificationsResult.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onDelete: () -> Unit, onMarkRead: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFE3F2FD)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                )
                notification.createdAt?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Row {
                if (!notification.isRead) {
                    TextButton(onClick = onMarkRead) {
                        Text("Mark Read", fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        NotificationsScreen(navController = rememberNavController())
    }
}
