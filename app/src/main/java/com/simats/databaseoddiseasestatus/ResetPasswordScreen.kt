package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Reset Password", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Create a new password for your account ($email)")
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = resetPasswordState) {
            is ResetPasswordResult.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            ResetPasswordResult.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            }
            else -> {}
        }

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                passwordError = null
            },
            label = { Text("New Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon"
                )
            },
            trailingIcon = {
                IconButton(onClick = { newPasswordVisibility = !newPasswordVisibility }) {
                    Icon(
                        imageVector = if (newPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (newPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordError = null
            },
            label = { Text("Confirm new password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon"
                )
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                    Icon(
                        imageVector = if (confirmPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null
        )

        passwordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
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
            modifier = Modifier.fillMaxWidth(),
            enabled = resetPasswordState !is ResetPasswordResult.Loading
        ) {
            Text(text = "Submit")
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
