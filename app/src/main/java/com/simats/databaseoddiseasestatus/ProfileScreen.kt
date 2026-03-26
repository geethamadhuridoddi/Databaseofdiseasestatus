package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@Composable
fun ProfileScreen(
    navController: NavController, 
    userEmail: String, 
    userName: String? = null,
    userId: Int = -1,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile
    val context = LocalContext.current

    LaunchedEffect(userEmail, userId) {
        viewModel.fetchProfile(userEmail, userId = if (userId != -1) userId else null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1F5FE))
    ) {
        // Header Bar (Reference from Login)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF03A9F4)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(48.dp)) // Placeholder to balance the row
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Circular Profile Section (Reference from Login logo)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color(0xFF03A9F4).copy(alpha = 0.1f), shape = CircleShape)
                    .padding(12.dp)
                    .background(Color(0xFF03A9F4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Display
            val fallbackName = userEmail.split("@").first().replaceFirstChar { it.uppercase() }
            
            val apiName = profile?.name?.takeIf { it.isNotBlank() } 
            
            val displayName = apiName
                ?: userName?.takeIf { it.isNotBlank() && it != "Doctor" } 
                ?: fallbackName
            
            Text(
                text = displayName, 
                fontSize = 26.sp, 
                fontWeight = FontWeight.Bold,
                color = Color(0xFF01579B)
            )
            
            Text(
                text = "Doctor", 
                fontSize = 18.sp, 
                color = Color(0xFF0288D1),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF03A9F4), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profile?.email ?: userEmail, 
                    fontSize = 16.sp, 
                    color = Color(0xFF01579B).copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))

            // Menu Buttons (Reference from Login Authenticate button)
            OutlinedButton(
                onClick = {
                    val currentApiName = profile?.name?.takeIf { it.isNotBlank() }
                    
                    val nameToEdit = currentApiName ?: userName?.takeIf { it.isNotBlank() && it != "Doctor" } ?: fallbackName
                    val email = profile?.email ?: userEmail
                    val encodedEmail = android.net.Uri.encode(email)
                    val encodedName = android.net.Uri.encode(nameToEdit)
                    navController.navigate("edit_profile/$encodedEmail/$encodedName?userId=$userId")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFF03A9F4)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.logout { success ->
                        if (success) {
                            navController.navigate("thank_you") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        ProfileScreen(navController = rememberNavController(), userEmail = "test@example.com")
    }
}
