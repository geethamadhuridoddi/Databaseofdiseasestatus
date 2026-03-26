package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(navController: NavController, email: String, authViewModel: AuthViewModel = viewModel()) {
    var otp by remember { mutableStateOf("") }
    val verifyOtpState by authViewModel.verifyOtpState.collectAsState()

    LaunchedEffect(verifyOtpState) {
        if (verifyOtpState is VerifyOtpResult.Success) {
            if (navController.graph != null) {
                navController.navigate("reset_password/$email")
                authViewModel.resetVerifyOtpState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1F5FE)) // Light blue background
    ) {
        // Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF03A9F4)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "MediTrack",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 48.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Logo Section
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color(0xFF03A9F4).copy(alpha = 0.1f), shape = CircleShape)
                    .padding(12.dp)
                    .background(Color(0xFF03A9F4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verify Icon",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verify OTP",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Enter the 6-digit OTP sent to\n$email",
                color = Color(0xFF01579B),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            when (val state = verifyOtpState) {
                is VerifyOtpResult.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                VerifyOtpResult.Loading -> {
                    CircularProgressIndicator(color = Color(0xFF03A9F4), modifier = Modifier.padding(bottom = 16.dp))
                }
                else -> {}
            }

            // Input Field
            TextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                placeholder = { Text("6-Digit OTP:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF01579B),
                    unfocusedTextColor = Color(0xFF01579B)
                ),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Authenticate (Verify) Button
            OutlinedButton(
                onClick = { 
                    if (otp.length == 6) {
                        authViewModel.verifyForgotOtp(mapOf("email" to email, "otp" to otp))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFF03A9F4)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(8.dp),
                enabled = verifyOtpState !is VerifyOtpResult.Loading
            ) {
                Text(
                    text = "Verify",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Didn't receive OTP? Resend",
                modifier = Modifier.clickable { 
                    authViewModel.forgotPassword(mapOf("email" to email))
                },
                color = Color(0xFF0288D1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    OtpVerificationScreen(rememberNavController(), "test@example.com")
}
