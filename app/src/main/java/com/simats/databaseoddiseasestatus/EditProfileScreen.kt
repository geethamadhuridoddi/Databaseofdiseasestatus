package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@Composable
fun EditProfileScreen(
    navController: NavController,
    userEmail: String,
    currentName: String,
    userId: Int = -1,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile
    var name by remember { mutableStateOf(currentName.ifBlank { userEmail.split("@").first().replaceFirstChar { it.uppercase() } }) }
    var emailValue by remember { mutableStateOf(userEmail) }
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchProfile(userEmail, userId = if (userId != -1) userId else null)
    }

    LaunchedEffect(profile) {
        profile?.let {
            it.name?.let { n -> if (n.isNotBlank()) name = n }
            it.email?.let { e -> if (e.isNotBlank()) emailValue = e }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
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
                    text = "Edit Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Input Fields (Styled like Login Screen)
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Name:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
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
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                placeholder = { Text("Email Address:", color = Color(0xFF03A9F4).copy(alpha = 0.7f)) },
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
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Save Button (Styled like Login Screen's Authenticate button)
            OutlinedButton(
                onClick = {
                    if (name.isBlank() || emailValue.isBlank()) {
                        Toast.makeText(context, "Name and Email are required", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    viewModel.updateProfile(userId = if (userId != -1) userId else null, name = name) { success ->
                        if (success) {
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.5.dp, Color(0xFF03A9F4)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF03A9F4))
                } else {
                    Text(
                        text = "Save Changes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        EditProfileScreen(
            navController = rememberNavController(),
            userEmail = "test@example.com",
            currentName = "John Doe"
        )
    }
}
