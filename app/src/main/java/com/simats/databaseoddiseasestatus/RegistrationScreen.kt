package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val registrationState by authViewModel.registrationState.collectAsState()

    fun validatePassword(pass: String): String? {
        if (pass.length < 8) return "Password must be at least 8 characters long."
        if (!pass.any { it.isUpperCase() }) return "Password must contain at least one uppercase letter."
        if (!pass.any { it.isDigit() }) return "Password must contain at least one number."
        if (!pass.any { !it.isLetterOrDigit() }) return "Password must contain at least one special character."
        return null
    }

    LaunchedEffect(registrationState) {
        if (registrationState is RegistrationResult.Success) {
            if (navController.graph != null) {
                navController.navigate("login") {
                    popUpTo("registration") { inclusive = true }
                }
                authViewModel.resetRegistrationState()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1F5FE))
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
                    modifier = Modifier.padding(end = 48.dp) // Balance the back button
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Logo Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF03A9F4).copy(alpha = 0.1f), shape = CircleShape)
                    .padding(8.dp)
                    .background(Color(0xFF03A9F4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Doctor Icon",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Join Us!",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = registrationState) {
                is RegistrationResult.Error -> {
                    Text(state.message, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }
                RegistrationResult.Loading -> {
                    CircularProgressIndicator(color = Color(0xFF03A9F4), modifier = Modifier.padding(bottom = 16.dp))
                }
                else -> {}
            }

            // Input Fields
            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("Full Name:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email Address:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = validatePassword(it)
                },
                placeholder = { Text("Password:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF03A9F4)
                        )
                    }
                }
            )
            passwordError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it != password) "Passwords do not match." else null
                },
                placeholder = { Text("Confirm Password:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF03A9F4)
                        )
                    }
                }
            )
            confirmPasswordError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }

            Spacer(modifier = Modifier.height(32.dp))

            // Authenticate (Sign Up) Button
            OutlinedButton(
                onClick = {
                    passwordError = validatePassword(password)
                    confirmPasswordError = if (password != confirmPassword) "Passwords do not match." else null
                    if (passwordError == null && confirmPasswordError == null) {
                        val userData = mapOf(
                            "full_name" to fullName,
                            "email" to email,
                            "password" to password,
                            "confirm_password" to confirmPassword
                        )
                        authViewModel.registerUser(userData)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFF03A9F4)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(8.dp),
                enabled = registrationState !is RegistrationResult.Loading
            ) {
                Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text("Already have an account? ", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "Login",
                    modifier = Modifier.clickable { 
                        if (navController.graph != null) {
                            navController.navigate("login") 
                        }
                    },
                    color = Color(0xFF0288D1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen(navController = rememberNavController())
}
