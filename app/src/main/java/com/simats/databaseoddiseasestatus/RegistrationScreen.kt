package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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

    // Safely handle navigation when registration is successful
    LaunchedEffect(registrationState) {
        if (registrationState is RegistrationResult.Success) {
            // Use try-catch or check for graph to prevent crash if navigated too early
            try {
                navController.navigate("login") {
                    popUpTo("registration") { inclusive = true }
                }
                authViewModel.resetRegistrationState()
            } catch (e: Exception) {
                // If graph is not set yet, we can't navigate. 
                // In a real app, you might want to retry or log this.
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "MediTrack Logo",
                    tint = Color(0xFF00A6A6),
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    text = "Sign Up to get started.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Fullname") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = validatePassword(it)
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, "toggle password visibility")
                        }
                    },
                    isError = passwordError != null
                )
                passwordError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = if (it != password) "Passwords do not match." else null
                    },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, "toggle confirm password visibility")
                        }
                    },
                    isError = confirmPasswordError != null
                )
                confirmPasswordError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))

                when (val state = registrationState) {
                    is RegistrationResult.Error -> {
                        Text(state.message, color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
                    }
                    RegistrationResult.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                    }
                    else -> {}
                }

                Button(
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A6A6)),
                    enabled = fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                ) {
                    Text("Sign Up", color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                val annotatedText = buildAnnotatedString {
                    append("Already have an account? ")
                    pushStringAnnotation(tag = "LOGIN", annotation = "login")
                    withStyle(style = SpanStyle(color = Color(0xFF00A6A6), textDecoration = TextDecoration.Underline)) {
                        append("Login")
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                            .firstOrNull()?.let {
                                try {
                                    navController.navigate("login")
                                } catch (e: Exception) {
                                    // Handle cases where graph is not yet set
                                }
                            }
                    }
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
