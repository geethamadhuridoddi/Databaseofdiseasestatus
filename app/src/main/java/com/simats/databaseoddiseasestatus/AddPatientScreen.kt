package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(navController: NavController, viewModel: PatientViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(30) }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val context = LocalContext.current

    val addPatientState by viewModel.addPatientState.collectAsState()

    LaunchedEffect(addPatientState) {
        when (addPatientState) {
            is AddPatientResult.Success -> {
                Toast.makeText(context, "Patient added successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetAddPatientState()
                navController.popBackStack()
            }
            is AddPatientResult.Error -> {
                Toast.makeText(context, (addPatientState as AddPatientResult.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetAddPatientState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Patient", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3F51B5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = age.toString(),
                    onValueChange = {},
                    label = { Text("Age *") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { if (age > 0) age-- }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease Age")
                            }
                            IconButton(onClick = { age++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase Age")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gender *", style = MaterialTheme.typography.bodySmall)
                    Row {
                        FilterChip(selected = gender == "Male", onClick = { gender = "Male" }, label = { Text("Male") })
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = gender == "Female", onClick = { gender = "Female" }, label = { Text("Female") })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address *") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            if (addPatientState is AddPatientResult.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || address.isBlank()) {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val patientData = mapOf(
                            "name" to name,
                            "age" to age,
                            "gender" to gender,
                            "phone_number" to phone,
                            "address" to address
                        )
                        viewModel.addPatient(patientData)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Save Patient", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPatientScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        AddPatientScreen(navController = rememberNavController())
    }
}
