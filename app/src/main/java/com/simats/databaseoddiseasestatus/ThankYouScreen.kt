package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@Composable
fun ThankYouScreen(navController: NavController) {
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
            Text(
                text = "MediTrack",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Logo Section (Reference from Login logo)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color(0xFF03A9F4).copy(alpha = 0.1f), shape = CircleShape)
                    .padding(12.dp)
                    .background(Color(0xFF03A9F4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Thank You!",
                color = Color(0xFF01579B),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your session has been saved successfully.\nThank you for using MediTrack.",
                color = Color(0xFF0288D1),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Return Button (Reference from Login Authenticate button)
            OutlinedButton(
                onClick = { 
                    if (navController.graph != null) {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFF03A9F4)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Return to Home",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThankYouScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        ThankYouScreen(navController = rememberNavController())
    }
}
