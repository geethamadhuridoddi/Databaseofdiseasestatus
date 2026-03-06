package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

// Color Palette
val indigoBlue = Color(0xFF3F51B5)
val lightBlue = Color(0xFFE3F2FD)
val lightPink = Color(0xFFFCE4EC)
val lightGreen = Color(0xFFE8F5E9)
val lightYellow = Color(0xFFFFF8E1)
val lightRed = Color(0xFFFFEBEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, 
    authViewModel: AuthViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Patients", "Diseases", "Reports", "Settings")
    val icons = listOf(Icons.Default.People, Icons.Default.MonitorHeart, Icons.Default.Assessment, Icons.Default.Settings)
    
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val dashboardState by authViewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.fetchDashboardStats()
        notificationViewModel.fetchUnreadCount()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Welcome back", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Row {
                    BadgedBox(badge = {
                        if (unreadCount > 0) {
                            Badge {
                                Text(unreadCount.toString())
                            }
                        }
                    }) {
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = indigoBlue)
                        }
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = indigoBlue)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (item) {
                                "Patients" -> navController.navigate("patients")
                                "Diseases" -> navController.navigate("diseases")
                                "Reports" -> navController.navigate("reports")
                                "Settings" -> navController.navigate("settings")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = indigoBlue,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = indigoBlue,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = lightBlue.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Handle chatbot click */ },
                containerColor = Color.Transparent, 
                contentColor = Color.White,
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp), spotColor = indigoBlue)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chatbot")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = listOf(lightBlue, lightPink)))
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = dashboardState) {
                is DashboardResult.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = indigoBlue)
                    }
                }
                is DashboardResult.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                is DashboardResult.Success -> {
                    val stats = state.response
                    val activeCases = stats.statusSummary.find { it.status == "Active" }?.count ?: 0
                    val recovering = stats.statusSummary.find { it.status == "Recovering" }?.count ?: 0
                    val critical = stats.statusSummary.find { it.status == "Critical" }?.count ?: 0

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DashboardCard(title = "Total Patients", value = stats.totalPatients.toString(), trend = "+13.9%", trendIcon = Icons.Default.ArrowUpward, iconColor = Color.Green, cardIcon = Icons.Default.People, modifier = Modifier.weight(1f), cardColor = lightBlue, iconBackgroundColor = Color(0xFF0D47A1), valueColor = Color(0xFF000080))
                        DashboardCard(title = "Active Cases", value = activeCases.toString(), trend = "+13.8%", trendIcon = Icons.Default.ArrowUpward, iconColor = Color.Green, cardIcon = Icons.Default.NorthEast, modifier = Modifier.weight(1f), cardColor = lightGreen, iconBackgroundColor = Color(0xFF1B5E20), valueColor = Color(0xFF004D40))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DashboardCard(title = "Recovering Patients", value = recovering.toString(), trend = "-2.0%", trendIcon = Icons.Default.ArrowDownward, iconColor = Color.Red, cardIcon = Icons.Default.Healing, modifier = Modifier.weight(1f), cardColor = lightYellow, iconBackgroundColor = Color(0xFFFF8F00), valueColor = Color(0xFFE65100))
                        DashboardCard(title = "Critical Cases", value = critical.toString(), trend = "+5.0%", trendIcon = Icons.Default.ArrowUpward, iconColor = Color.Green, cardIcon = Icons.Default.Warning, modifier = Modifier.weight(1f), cardColor = lightRed, iconBackgroundColor = Color(0xFFB71C1C), valueColor = Color(0xFFD50000))
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recovery & Critical Cases Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = indigoBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(
                                Color.LightGray.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        lightGreen.copy(alpha = 0.5f),
                                        lightRed.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chart will be displayed here", textAlign = TextAlign.Center, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patient Volume Trend", color = Color.DarkGray)
                        Text("+13.9%", fontWeight = FontWeight.Bold, color = Color.Green)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Disease Cases Trend", color = Color.DarkGray)
                        Text("+13.8%", fontWeight = FontWeight.Bold, color = Color(0xFF2962FF))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Recovery Rate", color = Color.DarkGray)
                        Text("23.0%", fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    trend: String,
    trendIcon: ImageVector,
    iconColor: Color,
    cardIcon: ImageVector,
    modifier: Modifier = Modifier,
    cardColor: Color,
    iconBackgroundColor: Color,
    valueColor: Color
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Icon(imageVector = cardIcon, contentDescription = null, tint = iconBackgroundColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = trendIcon, contentDescription = null, tint = iconColor)
                Text(text = trend, style = MaterialTheme.typography.bodySmall, color = iconColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
