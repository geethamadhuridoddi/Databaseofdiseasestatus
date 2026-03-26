package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPatientScreen(navController: NavController, patientId: String?, userId: Int = -1, viewModel: PatientViewModel = viewModel()) {
    val patient = globalPatients.find { it.id.toString() == patientId }
    val context = LocalContext.current
    val updateState by viewModel.updatePatientState.collectAsState()

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdatePatientResult.Success -> {
                Toast.makeText(context, "Patient updated successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdatePatientState()
                navController.popBackStack()
            }
            is UpdatePatientResult.Error -> {
                Toast.makeText(context, (updateState as UpdatePatientResult.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdatePatientState()
            }
            else -> {}
        }
    }

    if (patient == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Patient not found.")
        }
        return
    }

    var name by remember { mutableStateOf(patient.name ?: "") }
    var age by remember { mutableStateOf(patient.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(patient.gender ?: "") }
    var phone by remember { mutableStateOf(patient.displayPhone) }
    var address by remember { mutableStateOf(patient.address ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Patient", fontWeight = FontWeight.Bold) },
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
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            if (updateState is UpdatePatientResult.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val patientData = mutableMapOf<String, Any>(
                            "name" to name,
                            "age" to (age.toIntOrNull() ?: 0),
                            "gender" to gender,
                            "phone_number" to phone,
                            "phone" to phone,
                            "phoneNumber" to phone,
                            "phone_no" to phone,
                            "phoneNo" to phone,
                            "mobile" to phone,
                            "contact" to phone,
                            "address" to address
                        )
                        if (userId != -1) {
                            patientData["user_id"] = userId
                        }
                        patientId?.let { viewModel.updatePatient(it, patientData, userId = if (userId != -1) userId else null) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Update Patient", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPatientScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        EditPatientScreen(navController = rememberNavController(), patientId = "YJ-555 0101")
    }
}
