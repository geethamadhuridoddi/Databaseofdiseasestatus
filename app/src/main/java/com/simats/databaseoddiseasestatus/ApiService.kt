package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

data class RegistrationResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("patient_id")
    val patientId: Int? = null
)

data class LoginResponse(
    val message: String? = null,
    val error: String? = null
)

data class GenericResponse(
    val message: String? = null,
    val error: String? = null
)

data class AssignDiseaseResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("record_id")
    val recordId: Int? = null
)

data class UpdatePatientDiseaseResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("patient_id")
    val patientId: Int? = null,
    @SerializedName("patient_name")
    val patientName: String? = null,
    val disease: String? = null,
    @SerializedName("new_status")
    val newStatus: String? = null,
    @SerializedName("new_severity")
    val newSeverity: String? = null
)

data class DashboardResponse(
    @SerializedName("total_patients")
    val totalPatients: Int,
    @SerializedName("total_diseases")
    val totalDiseases: Int,
    @SerializedName("status_summary")
    val statusSummary: List<StatusCount>,
    @SerializedName("disease_summary")
    val diseaseSummary: List<DiseaseCount>
)

data class StatusCount(
    val status: String,
    val count: Int
)

data class DiseaseCount(
    @SerializedName("disease__name")
    val diseaseName: String,
    val count: Int
)

data class DiseaseCatalogItem(
    val id: Int,
    val name: String,
    @SerializedName("default_severity")
    val defaultSeverity: String
)

// --- Report Data Classes ---
data class ReportSummaryResponse(
    @SerializedName("total_cases")
    val totalCases: Int,
    @SerializedName("recovery_rate")
    val recoveryRate: Double
)

data class PatientReportItem(
    val name: String,
    val age: Int,
    val diseases: Int
)

data class DiseaseAnalyticsResponse(
    @SerializedName("status_distribution")
    val statusDistribution: Map<String, Int>,
    @SerializedName("severity_levels")
    val severityLevels: Map<String, Int>
)

// --- Settings Data Classes ---
data class SettingsResponse(
    val theme: String,
    val language: String
)

data class SaveSettingsResponse(
    val message: String,
    val settings: SettingsResponse
)

data class ActivityLogItem(
    val message: String
)

// --- Profile Data Classes ---
data class ProfileResponse(
    val name: String,
    val role: String,
    val phone: String
)

interface ApiService {
    @POST("register/")
    fun registerUser(@Body userData: Map<String, @JvmSuppressWildcards Any>): Call<RegistrationResponse>

    @POST("login/")
    fun loginUser(@Body credentials: Map<String, String>): Call<LoginResponse>

    @POST("forgot-password/")
    fun forgotPassword(@Body emailData: Map<String, String>): Call<GenericResponse>

    @POST("verify-forgot-otp/")
    fun verifyForgotOtp(@Body otpData: Map<String, String>): Call<GenericResponse>

    @POST("reset-password/")
    fun resetPassword(@Body resetData: Map<String, String>): Call<GenericResponse>

    // --- Patient Endpoints ---
    @POST("patients/add/")
    fun addPatient(@Body patientData: Map<String, @JvmSuppressWildcards Any>): Call<RegistrationResponse>

    @GET("patients/")
    fun getPatients(): Call<List<Patient>>

    @PUT("patients/update/{patient_id}/")
    fun updatePatient(
        @Path("patient_id") patientId: Int,
        @Body patientData: Map<String, @JvmSuppressWildcards Any>
    ): Call<GenericResponse>

    @DELETE("patients/delete/{patient_id}/")
    fun deletePatient(@Path("patient_id") patientId: Int): Call<GenericResponse>

    @POST("patients/assign-disease/")
    fun assignDisease(@Body diseaseData: Map<String, @JvmSuppressWildcards Any>): Call<AssignDiseaseResponse>

    @PUT("patients/disease/{record_id}/update/")
    fun updatePatientDiseaseStatus(
        @Path("record_id") recordId: Int,
        @Body updateData: Map<String, String>
    ): Call<UpdatePatientDiseaseResponse>

    // --- Disease Endpoints ---
    @POST("diseases/add/")
    fun addDisease(@Body diseaseData: Map<String, @JvmSuppressWildcards Any>): Call<GenericResponse>

    @GET("diseases/")
    fun getDiseases(): Call<List<DiseaseCatalogItem>>

    @PUT("diseases/{disease_id}/update/")
    fun updateDisease(
        @Path("disease_id") diseaseId: Int,
        @Body diseaseData: Map<String, @JvmSuppressWildcards Any>
    ): Call<GenericResponse>

    @DELETE("diseases/{disease_id}/delete/")
    fun deleteDisease(@Path("disease_id") diseaseId: Int): Call<GenericResponse>

    // --- Dashboard & Notifications ---
    @GET("dashboard/")
    fun getDashboardStats(): Call<DashboardResponse>

    @GET("notifications/")
    fun getNotifications(): Call<List<Notification>>

    @GET("notifications/unread-count/")
    fun getUnreadCount(): Call<Map<String, Int>>

    @PATCH("notifications/{notification_id}/read/")
    fun markAsRead(@Path("notification_id") notificationId: Int): Call<GenericResponse>

    @DELETE("notifications/{notification_id}/delete/")
    fun deleteNotification(@Path("notification_id") notificationId: Int): Call<GenericResponse>

    @GET("patients/filter/")
    fun filterByStatus(@Query("status") status: String): Call<List<Patient>>

    // --- Report Endpoints ---
    @GET("reports/summary/")
    fun getReportSummary(): Call<ReportSummaryResponse>

    @GET("reports/patients/")
    fun getPatientReport(): Call<List<PatientReportItem>>

    @GET("reports/analytics/")
    fun getDiseaseAnalytics(): Call<DiseaseAnalyticsResponse>

    @GET("reports/download/")
    @Streaming
    fun downloadReport(@Query("format") format: String): Call<ResponseBody>

    // --- Settings & Activity Log Endpoints ---
    @GET("settings/")
    fun getSettings(): Call<SettingsResponse>

    @POST("settings/save/")
    fun saveSettings(@Body settingsData: Map<String, String>): Call<SaveSettingsResponse>

    @GET("activity-log/")
    fun getActivityLog(): Call<List<ActivityLogItem>>

    // --- Profile Endpoints ---
    @GET("profile/")
    fun getProfile(): Call<ProfileResponse>

    @POST("profile/update/")
    fun updateProfile(@Body profileData: Map<String, String>): Call<GenericResponse>

    @POST("logout/")
    fun logout(): Call<GenericResponse>
}
