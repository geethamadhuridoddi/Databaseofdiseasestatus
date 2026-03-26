package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
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
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController, email: String, authViewModel: AuthViewModel = viewModel()) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var newPasswordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    val resetPasswordState by authViewModel.resetPasswordState.collectAsState()

    LaunchedEffect(resetPasswordState) {
        if (resetPasswordState is ResetPasswordResult.Success) {
            if (navController.graph != null) {
                navController.navigate("login") {
                    popUpTo("forgot_password") { inclusive = true }
                }
                authViewModel.resetResetPasswordState()
            }
        }
    }

    fun validatePassword(password: String): String? {
        if (password.length < 8) {
            return "Password must be at least 8 characters long."
        }
        if (!password.any { it.isUpperCase() }) {
            return "Password must contain at least one uppercase letter."
        }
        if (!password.any { it.isLowerCase() }) {
            return "Password must contain at least one lowercase letter."
        }
        if (!password.any { it.isDigit() }) {
            return "Password must contain at least one number."
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return "Password must contain at least one special character."
        }
        return null
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
                        imageVector = Icons.Default.Password,
                        contentDescription = "Reset Icon",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New Password",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Set a secure new password for\n$email",
                color = Color(0xFF01579B),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            when (val state = resetPasswordState) {
                is ResetPasswordResult.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                ResetPasswordResult.Loading -> {
                    CircularProgressIndicator(color = Color(0xFF03A9F4), modifier = Modifier.padding(bottom = 16.dp))
                }
                else -> {}
            }

            // Input Fields
            TextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    passwordError = null
                },
                placeholder = { Text("New Password:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                visualTransformation = if (newPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF01579B),
                    unfocusedTextColor = Color(0xFF01579B)
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisibility = !newPasswordVisibility }) {
                        Icon(
                            imageVector = if (newPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF03A9F4)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    passwordError = null
                },
                placeholder = { Text("Confirm Password:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB3E5FC),
                    unfocusedContainerColor = Color(0xFFB3E5FC),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF01579B),
                    unfocusedTextColor = Color(0xFF01579B)
                ),
                shape = RoundedCornerShape(4.dp),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(
                            imageVector = if (confirmPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF03A9F4)
                        )
                    }
                }
            )

            passwordError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            OutlinedButton(
                onClick = {
                    if (newPassword != confirmPassword) {
                        passwordError = "Passwords do not match."
                    } else {
                        val error = validatePassword(newPassword)
                        if (error != null) {
                            passwordError = error
                        } else {
                            authViewModel.resetPassword(mapOf(
                                "email" to email,
                                "new_password" to newPassword,
                                "confirm_password" to confirmPassword
                            ))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFF03A9F4)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(8.dp),
                enabled = resetPasswordState !is ResetPasswordResult.Loading
            ) {
                Text(
                    text = "Update Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        ResetPasswordScreen(rememberNavController(), "test@example.com")
    }
}
