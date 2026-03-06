package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Verify OTP",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Enter the OTP sent to $email",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = Color.Gray
                )

                when (val state = verifyOtpState) {
                    is VerifyOtpResult.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    VerifyOtpResult.Loading -> {
                        CircularProgressIndicator(color = Color(0xFF00A6A6), modifier = Modifier.padding(bottom = 16.dp))
                    }
                    else -> {}
                }

                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("Enter OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        if (otp.length == 6) {
                            authViewModel.verifyForgotOtp(mapOf("email" to email, "otp" to otp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A6A6)),
                    enabled = verifyOtpState !is VerifyOtpResult.Loading
                ) {
                    Text("Verify", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { 
                    authViewModel.forgotPassword(mapOf("email" to email))
                }) {
                    Text("Didn't receive OTP? Resend", color = Color(0xFF00A6A6))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    OtpVerificationScreen(rememberNavController(), "test@example.com")
}
