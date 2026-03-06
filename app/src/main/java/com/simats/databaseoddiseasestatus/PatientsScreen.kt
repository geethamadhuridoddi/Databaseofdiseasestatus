package com.simats.databaseoddiseasestatus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(navController: NavController, viewModel: PatientViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf<String?>("All") }

    val patientsState by viewModel.patientsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPatients()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_patient") },
                containerColor = Color(0xFF3F51B5),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search patients by name...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            var expanded by remember { mutableStateOf(false) }
            val items = listOf("All", "Male", "Female")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                TextField(
                    readOnly = true,
                    value = selectedSex ?: "All",
                    onValueChange = {},
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                selectedSex = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = patientsState) {
                    is PatientsResult.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is PatientsResult.Success -> {
                        val filteredPatients = state.patients.filter { patient ->
                            val nameMatches = patient.name.contains(searchQuery, ignoreCase = true)
                            val sexMatches = selectedSex == "All" || patient.gender.equals(selectedSex, ignoreCase = true)
                            nameMatches && sexMatches
                        }
                        
                        if (filteredPatients.isEmpty()) {
                            Text(
                                text = "No patients found.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 8.dp)
                            ) {
                                items(filteredPatients) { patient ->
                                    PatientListItem(patient = patient, onClick = {
                                        navController.navigate("patient_details/${patient.id}")
                                    })
                                }
                            }
                        }
                    }
                    is PatientsResult.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun PatientListItem(patient: Patient, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(patient.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${patient.age} years  •  ${patient.gender}  •  ${patient.diseases?.size ?: 0} diseases")
            patient.id?.let { Text(it) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatientsScreenPreview() {
    DatabaseOdDiseaseStatusTheme {
        PatientsScreen(navController = rememberNavController())
    }
}
