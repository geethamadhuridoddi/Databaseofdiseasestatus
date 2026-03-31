package com.simats.databaseoddiseasestatus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DatabaseOdDiseaseStatusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(3000)
                navController.navigate("welcome") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("welcome") {
            WelcomeScreen(navController)
        }
        composable("onboarding") {
            OnboardingScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegistrationScreen(navController)
        }
        composable("thank_you") {
            ThankYouScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable(
            "otp_verification/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(navController, email)
        }
        composable(
            "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(navController, email)
        }
        composable(
            "dashboard/{email}?userName={userName}&userId={userId}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType; nullable = true },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val name = backStackEntry.arguments?.getString("userName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            DashboardScreen(navController, email, name, userId = userId)
        }
        composable(
            "patients?doctorName={doctorName}&filter={filter}&userId={userId}",
            arguments = listOf(
                navArgument("doctorName") { type = NavType.StringType; nullable = true },
                navArgument("filter") { type = NavType.StringType; nullable = true },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val doctorName = backStackEntry.arguments?.getString("doctorName")
            val filter = backStackEntry.arguments?.getString("filter")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            PatientsScreen(navController, doctorName = doctorName, initialFilter = filter, userId = userId)
        }
        composable(
            "add_patient?doctorName={doctorName}&userId={userId}",
            arguments = listOf(
                navArgument("doctorName") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val doctorName = backStackEntry.arguments?.getString("doctorName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            AddPatientScreen(navController, doctorName = doctorName, userId = userId)
        }
        composable("diseases?userId={userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            DiseasesScreen(navController, userId = userId)
        }
        composable("reports?userId={userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            ReportsScreen(navController, userId = userId)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("notifications") {
            NotificationsScreen(navController)
        }
        composable(
            "profile/{email}?userName={userName}&userId={userId}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType; nullable = true },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val name = backStackEntry.arguments?.getString("userName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            ProfileScreen(navController, email, name, userId = userId)
        }
        composable(
            "edit_profile/{email}/{name}?userId={userId}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            EditProfileScreen(navController, email, name, userId = userId)
        }
        composable("patient_report?userId={userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            PatientReportScreen(navController, userId = userId)
        }
        composable("download_report?userId={userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            DownloadReportScreen(navController, userId = userId)
        }

        // --- Patient & Disease Management Routes ---
        composable(
            "patient_details/{patientId}?doctorName={doctorName}&userId={userId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("doctorName") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val doctorName = backStackEntry.arguments?.getString("doctorName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            PatientDetailsScreen(navController, patientId, doctorName, userId = userId)
        }
        composable(
            "edit_patient/{patientId}?userId={userId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            EditPatientScreen(navController, patientId, userId = userId)
        }
        composable(
            "add_disease/{patientId}?doctorName={doctorName}&userId={userId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("doctorName") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val doctorName = backStackEntry.arguments?.getString("doctorName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            AddDiseaseScreen(navController, patientId, doctorName, userId = userId)
        }
        composable(
            "disease_details/{patientId}/{diseaseId}?userId={userId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("diseaseId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val diseaseId = backStackEntry.arguments?.getString("diseaseId")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            DiseaseDetailsScreen(navController, patientId, diseaseId, userId = userId)
        }
        composable(
            "disease_details_record/{recordId}?userId={userId}",
            arguments = listOf(
                navArgument("recordId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            DiseaseDetailsByRecordScreen(navController, recordId, userId = userId)
        }
        composable(
            "update_status/{patientId}/{diseaseId}?recordId={recordId}&userId={userId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("diseaseId") { type = NavType.StringType },
                navArgument("recordId") { type = NavType.StringType; nullable = true },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val diseaseId = backStackEntry.arguments?.getString("diseaseId")
            val recordId = backStackEntry.arguments?.getString("recordId")?.toIntOrNull()
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            UpdateStatusScreen(navController, patientId, diseaseId, recordId, userId = userId)
        }
        composable(
            "update_status_direct/{recordId}?userId={userId}",
            arguments = listOf(
                navArgument("recordId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getInt("recordId")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            UpdateStatusScreen(navController, null, null, recordId, userId = userId)
        }
        composable("disease_history/{patientId}?diseaseName={diseaseName}&userId={userId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("diseaseName") { type = NavType.StringType; nullable = true },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val diseaseName = backStackEntry.arguments?.getString("diseaseName")
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            DiseaseHistoryScreen(navController, patientId, diseaseName, userId = userId)
        }
    }
}
