package com.simats.databaseoddiseasestatus

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(
    navController: NavController,
    doctorName: String? = null,
    userId: Int = -1,
    viewModel: PatientViewModel = viewModel(),
    diseaseViewModel: DiseaseViewModel = viewModel()
) {

    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }   // ✅ fixed
    var gender by remember { mutableStateOf("") }    // ✅ fixed
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val context = LocalContext.current
    val nameToUse = doctorName?.takeIf { it.isNotBlank() } ?: "Doctor"

    val addPatientState by viewModel.addPatientState.collectAsState()
    val addDiseaseState by diseaseViewModel.addDiseaseState.collectAsState()

    var isLinking by remember { mutableStateOf(false) }

    // Reset states
    LaunchedEffect(Unit) {
        viewModel.resetAddPatientState()
        diseaseViewModel.resetAddDiseaseState()
    }

    // Handle patient response
    LaunchedEffect(addPatientState) {
        when (addPatientState) {

            is AddPatientResult.Success -> {
                val newPatientId =
                    (addPatientState as AddPatientResult.Success).response.patientId

                if (newPatientId != null) {
                    isLinking = true

                    val diseaseData = mapOf(
                        "patient_id" to newPatientId,
                        "disease_name" to "General Consultation",
                        "status" to "Active",
                        "severity" to "Medium",
                        "diagnosis_date" to SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                        "doctor" to nameToUse,
                        "notes" to "Automatically assigned upon patient registration."
                    )

                    diseaseViewModel.assignDisease(diseaseData)

                } else {
                    Toast.makeText(context, "Patient added successfully", Toast.LENGTH_SHORT).show()
                    viewModel.resetAddPatientState()
                    navController.popBackStack()
                }
            }

            is AddPatientResult.Error -> {
                val rawError = (addPatientState as AddPatientResult.Error).message

                val displayError = try {
                    when {
                        rawError.contains("\"error\"") ->
                            rawError.substringAfter("\"error\": \"").substringBefore("\"")

                        rawError.contains("\"message\"") ->
                            rawError.substringAfter("\"message\": \"").substringBefore("\"")

                        else -> rawError
                    }
                } catch (e: Exception) {
                    rawError
                }

                Toast.makeText(context, displayError, Toast.LENGTH_LONG).show()
                viewModel.resetAddPatientState()
            }

            else -> {}
        }
    }

    // Handle disease linking
    LaunchedEffect(addDiseaseState) {
        if (isLinking) {
            when (addDiseaseState) {

                is AddDiseaseResult.Success -> {
                    Toast.makeText(
                        context,
                        "Patient added and linked successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    isLinking = false
                    viewModel.resetAddPatientState()
                    diseaseViewModel.resetAddDiseaseState()
                    navController.popBackStack()
                }

                is AddDiseaseResult.Error -> {
                    val rawError = (addDiseaseState as AddDiseaseResult.Error).message

                    Toast.makeText(
                        context,
                        "Patient added but linking failed: $rawError",
                        Toast.LENGTH_LONG
                    ).show()

                    isLinking = false
                    viewModel.resetAddPatientState()
                    diseaseViewModel.resetAddDiseaseState()
                    navController.popBackStack()
                }

                else -> {}
            }
        }
    }

    // UI
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
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Age + Gender
            Row(verticalAlignment = Alignment.CenterVertically) {

                OutlinedTextField(
                    value = ageText,
                    onValueChange = {
                        if (it.all { ch -> ch.isDigit() }) ageText = it
                    },
                    label = { Text("Age *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                val current = ageText.toIntOrNull() ?: 0
                                if (current > 0) ageText = (current - 1).toString()
                            }) {
                                Icon(Icons.Default.Remove, null)
                            }
                            IconButton(onClick = {
                                val current = ageText.toIntOrNull() ?: 0
                                ageText = (current + 1).toString()
                            }) {
                                Icon(Icons.Default.Add, null)
                            }
                        }
                    }
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Gender *")

                    Row {
                        FilterChip(
                            selected = gender == "Male",
                            onClick = { gender = "Male" },
                            label = { Text("Male") }
                        )

                        Spacer(Modifier.width(8.dp))

                        FilterChip(
                            selected = gender == "Female",
                            onClick = { gender = "Female" },
                            label = { Text("Female") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address *") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(32.dp))

            // Loading
            if (addPatientState is AddPatientResult.Loading ||
                addDiseaseState is AddDiseaseResult.Loading || isLinking
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                if (isLinking) {
                    Text(
                        "Linking patient...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

            } else {

                // Save Button
                Button(
                    onClick = {

                        val age = ageText.toIntOrNull()

                        if (name.isBlank() || age == null || phone.isBlank()
                            || address.isBlank() || gender.isBlank()
                        ) {
                            Toast.makeText(
                                context,
                                "Fill all fields correctly",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (phone.length < 10) {
                            Toast.makeText(
                                context,
                                "Enter valid phone",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val patientData = mapOf(
                            "name" to name,
                            "age" to age,
                            "gender" to gender,
                            "phone_number" to phone,
                            "address" to address,
                            "user_id" to if (userId != -1) userId else ""
                        )

                        viewModel.addPatient(patientData)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLinking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5)
                    )
                ) {
                    Text("Save Patient", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreen() {
    DatabaseOdDiseaseStatusTheme {
        AddPatientScreen(navController = rememberNavController())
    }
}
