package com.simats.databaseoddiseasestatus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.databaseoddiseasestatus.ui.theme.DatabaseOdDiseaseStatusTheme
import kotlinx.coroutines.delay
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTheme by themeState
            val useDarkTheme = when (selectedTheme) {
                Theme.Light -> false
                Theme.Dark -> true
                Theme.System -> isSystemInDarkTheme()
            }

            DatabaseOdDiseaseStatusTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen()
                        LaunchedEffect(Unit) {
                            delay(2000)
                            navController.navigate("welcome")
                        }
                    }
                    composable("welcome") {
                        WelcomeScreen(navController = navController)
                    }
                    composable("onboarding") {
                        OnboardingScreen(navController = navController)
                    }
                    composable("role_selection") {
                        RoleSelectionScreen(navController = navController)
                    }
                    composable("login") {
                        LoginScreen(navController = navController)
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(navController = navController)
                    }
                    composable(
                        "otp_verification/{email}",
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        OtpVerificationScreen(navController = navController, email = email)
                    }
                    composable(
                        "reset_password/{email}",
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        ResetPasswordScreen(navController = navController, email = email)
                    }
                    composable("dashboard") {
                        DashboardScreen(navController = navController)
                    }
                    composable("notifications") {
                        NotificationsScreen(navController = navController)
                    }
                    composable("profile") {
                        ProfileScreen(navController = navController)
                    }
                    composable(
                        "edit_profile/{name}/{phone}",
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("phone") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val phone = backStackEntry.arguments?.getString("phone") ?: ""
                        EditProfileScreen(
                            navController = navController,
                            currentName = name,
                            currentPhone = phone
                        )
                    }
                    composable("thank_you") {
                        ThankYouScreen(navController = navController)
                    }
                    composable("patients") {
                        PatientsScreen(navController = navController)
                    }
                    composable("add_patient") {
                        AddPatientScreen(navController = navController)
                    }
                    composable(
                        "patient_details/{patientId}",
                        arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        PatientDetailsScreen(
                            navController = navController,
                            patientId = backStackEntry.arguments?.getString("patientId")
                        )
                    }
                    composable(
                        "edit_patient/{patientId}",
                        arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        EditPatientScreen(
                            navController = navController,
                            patientId = backStackEntry.arguments?.getString("patientId")
                        )
                    }
                    composable(
                        "add_disease/{patientId}",
                        arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        AddDiseaseScreen(
                            navController = navController,
                            patientId = backStackEntry.arguments?.getString("patientId")
                        )
                    }
                    composable(
                        "disease_history/{patientId}",
                        arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        DiseaseHistoryScreen(
                            navController = navController,
                            patientId = backStackEntry.arguments?.getString("patientId")
                        )
                    }
                    composable("diseases") {
                        DiseasesScreen(navController = navController)
                    }
                    composable(
                        "disease_details/{patientId}/{diseaseId}",
                        arguments = listOf(
                            navArgument("patientId") { type = NavType.StringType },
                            navArgument("diseaseId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        DiseaseDetailsScreen(
                            navController = navController,
                            patientId = backStackEntry.arguments?.getString("patientId"),
                            diseaseId = backStackEntry.arguments?.getString("diseaseId")
                        )
                    }
                    composable(
                        "update_status/{patientId}/{diseaseId}",
                        arguments = listOf(
                            navArgument("patientId") { type = NavType.StringType },
                            navArgument("diseaseId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        UpdateStatusScreen(
                            navController = navController,
                            patientId = backStackEntry.arguments?.getString("patientId"),
                            diseaseId = backStackEntry.arguments?.getString("diseaseId")
                        )
                    }
                    composable("reports") {
                        ReportsScreen(navController = navController)
                    }
                    composable("patient_report") {
                        PatientReportScreen(navController = navController)
                    }
                    composable("disease_analytics") {
                        DiseaseAnalyticsScreen(navController = navController)
                    }
                    composable("download_report") {
                        DownloadReportScreen(navController = navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController = navController)
                    }
                    composable("activity_log") {
                        ActivityLogScreen(navController = navController)
                    }
                    composable("registration") {
                        RegistrationScreen(navController = navController)
                    }
                }
            }
        }
    }
}
