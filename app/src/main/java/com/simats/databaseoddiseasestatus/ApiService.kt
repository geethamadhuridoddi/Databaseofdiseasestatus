package com.simats.databaseoddiseasestatus

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import okhttp3.RequestBody
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.http.*
import com.simats.databaseoddiseasestatus.Patient
import com.simats.databaseoddiseasestatus.Disease
import com.simats.databaseoddiseasestatus.Notification
import com.simats.databaseoddiseasestatus.ActivityLogItem

data class RegistrationResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("id", alternate = ["patient_id", "pk", "userId"])
    val patientId: Int? = null
)

data class LoginResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("email", alternate = ["username", "user_email", "login_id"])
    val email: String? = null,
    val name: String? = null,
    @SerializedName("user_id", alternate = ["id", "pk", "userId", "user"])
    val userId: Int? = null
)

data class GenericResponse(
    val message: String? = null,
    val error: String? = null
)

data class AssignDiseaseResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("record_id", alternate = ["id", "pk"])
    val recordId: Int? = null
)

data class UpdatePatientDiseaseResponse(
    val message: String? = null,
    val error: String? = null,
    @SerializedName("patient_id", alternate = ["id", "pk"])
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
    @SerializedName("total_patients", alternate = ["patients_count", "total_pats"])
    val totalPatients: Int,
    @SerializedName("total_diseases", alternate = ["diseases_count", "total_dis"])
    val totalDiseases: Int,
    @SerializedName("total_cases", alternate = ["cases_count", "total_records", "total_cases_count", "total_cases_assigned"])
    val totalCases: Int,
    @SerializedName("active_cases", alternate = ["active_count", "active", "total_active"])
    val activeCases: Int,
    @SerializedName("recovering_cases", alternate = ["recovering_count", "recovering", "total_recovering"])
    val recoveringCases: Int,
    @SerializedName("critical_cases", alternate = ["critical_count", "critical", "total_critical"])
    val criticalCases: Int,
    @SerializedName("status_summary", alternate = ["status_counts", "stats"])
    val statusSummary: List<StatusCount>? = null,
    @SerializedName("disease_summary", alternate = ["disease_counts", "dis_stats"])
    val diseaseSummary: List<DiseaseCount>? = null
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
    @SerializedName("id", alternate = ["record_id", "pk", "disease_id"])
    val recordId: Int?,
    
    @SerializedName("disease_name", alternate = ["display_name", "disease_display_name"])
    val diseaseName: String? = null,
    
    @SerializedName("disease")
    val disease: String? = null,
    
    @SerializedName("name", alternate = ["alternate_name"])
    val aliasName: String? = null,
    
    @SerializedName("status", alternate = ["current_status", "condition", "patient_status"])
    val status: String? = null,
    
    @SerializedName("patient_id")
    val patientId: String? = null,
    
    @SerializedName("patient_name", alternate = ["patient_display_name", "patient", "user_name"])
    val patientName: String? = null,
    
    @SerializedName("doctor", alternate = ["assigned_doctor", "doctor_name", "primary_doctor", "doctor_display_name", "staff_name"])
    val doctor: String? = null,
    
    @SerializedName("severity", alternate = ["level", "case_severity"])
    val severity: String? = null,
    
    @SerializedName("diagnosis_date", alternate = ["date"])
    val diagnosisDate: String? = null,

    val notes: String? = null,
    @SerializedName("default_severity")
    val defaultSeverity: String? = null
) {
    val displayName: String 
        get() = diseaseName ?: disease ?: aliasName ?: "Unknown Disease"
}

// --- Report Data Classes ---
data class ReportSummaryResponse(
    @SerializedName("total_cases")
    val totalCases: Int,
    @SerializedName("recovery_rate")
    val recoveryRate: Double
)

data class PatientReportItem(
    @SerializedName("id", alternate = ["patient_id", "pk"])
    val id: String? = null,
    @SerializedName("name", alternate = ["patient_name", "username"])
    val name: String,
    @SerializedName("age", alternate = ["patient_age"])
    val age: String? = null,
    @SerializedName("diseases_count", alternate = ["disease_count", "total_diseases", "count", "num_diseases", "total", "disease_name", "num_cases", "disease_count_total"])
    val diseases: Int? = null
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

// ActivityLogItem is defined in ActivityLogItem.kt

// --- Profile Data Classes ---
data class ProfileResponse(
    @SerializedName("username", alternate = ["user_email"])
    val username: String? = null,
    val name: String? = null,
    val role: String? = null,
    @SerializedName("email", alternate = ["username"])
    val email: String? = null,
    @SerializedName("phone", alternate = ["phone_number", "contact"])
    val phone: String? = null
)

interface ApiService {
    @POST("register/")
    fun registerUser(@Body body: RequestBody): Call<ResponseBody>

    @POST("login/")
    fun loginUser(@Body body: RequestBody): Call<ResponseBody>

    @POST("forgot-password/")
    fun forgotPassword(@Body body: RequestBody): Call<ResponseBody>

    @POST("verify-forgot-otp/")
    fun verifyForgotOtp(@Body body: RequestBody): Call<ResponseBody>

    @POST("reset-password/")
    fun resetPassword(@Body body: RequestBody): Call<ResponseBody>

    // --- Patient Endpoints ---
    @POST("patients/add/")
    fun addPatient(@Body body: RequestBody): Call<ResponseBody>

    @GET("patients/")
    fun getPatients(
        @Query("status") status: String? = null,
        @Query("user_id") userId: Int? = null
    ): Call<ResponseBody>

    @GET("patients/{patient_id}/")
    fun getPatient(@Path("patient_id") patientId: String, @Query("user_id") userId: Int?): Call<ResponseBody>

    @PUT("patients/{patient_id}/update/")
    fun updatePatient(
        @Path("patient_id") patientId: String,
        @Body body: RequestBody
    ): Call<ResponseBody>

    @DELETE("patients/{patient_id}/delete/")
    fun deletePatient(@Path("patient_id") patientId: String, @Query("user_id") userId: Int?): Call<ResponseBody>

    @POST("patients/assign-disease/")
    fun assignDisease(@Body body: RequestBody): Call<ResponseBody>

    @PUT("patients/disease/{record_id}/update/")
    fun updatePatientDiseaseStatus(
        @Path("record_id") recordId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    @GET("diseases/{record_id}/detail/")
    fun getDiseaseRecord(@Path("record_id") recordId: Int, @Query("user_id") userId: Int? = null): Call<ResponseBody>

    @DELETE("diseases/{record_id}/delete/")
    fun deletePatientDisease(@Path("record_id") recordId: Int, @Query("user_id") userId: Int? = null): Call<ResponseBody>

    @GET("patients/{patient_id}/history/")
    fun getPatientHistory(@Path("patient_id") patientId: String, @Query("user_id") userId: Int?): Call<ResponseBody>

    // --- Disease Endpoints ---
    @POST("diseases/add/")
    fun addDisease(@Body body: RequestBody): Call<ResponseBody>

    @GET("diseases/")
    fun getDiseases(@Query("user_id") userId: Int? = null): Call<ResponseBody>

    @PUT("diseases/{disease_id}/update/")
    fun updateDisease(
        @Path("disease_id") diseaseId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    @DELETE("diseases/{disease_id}/delete/")
    fun deleteDisease(@Path("disease_id") diseaseId: Int): Call<ResponseBody>

    // --- Dashboard & Notifications ---
    @GET("dashboard/")
    fun getDashboardStats(@Query("user_id") userId: Int? = null): Call<ResponseBody>

    @GET("notifications/")
    fun getNotifications(): Call<ResponseBody>

    @GET("notifications/unread-count/")
    fun getUnreadCount(): Call<ResponseBody>

    @PATCH("notifications/{notification_id}/read/")
    fun markAsRead(@Path("notification_id") notificationId: Int): Call<ResponseBody>

    @DELETE("notifications/{notification_id}/delete/")
    fun deleteNotification(@Path("notification_id") notificationId: Int): Call<ResponseBody>

    // --- Report Endpoints ---
    @GET("reports/summary/")
    fun getReportSummary(@Query("user_id") userId: Int? = null): Call<ResponseBody>

    @GET("reports/patients/")
    fun getPatientReport(@Query("user_id") userId: Int? = null): Call<ResponseBody>

    @GET("reports/analytics/")
    fun getDiseaseAnalytics(@Query("user_id") userId: Int? = null): Call<ResponseBody>

    @GET("reports/download/")
    @Streaming
    fun downloadReport(
        @Query("format") format: String,
        @Query("user_id") userId: Int? = null
    ): Call<ResponseBody>

    // --- Settings & Activity Log Endpoints ---
    @GET("settings/")
    fun getSettings(): Call<ResponseBody>

    @POST("settings/save/")
    fun saveSettings(@Body body: RequestBody): Call<ResponseBody>

    @GET("activity-log/")
    fun getActivityLog(): Call<ResponseBody>

    @GET("profile/")
    fun getProfile(@Query("user_id") userId: Int?): Call<ResponseBody>

    @POST("profile/update/")
    fun updateProfile(@Body body: RequestBody): Call<ResponseBody>

    @POST("logout/")
    fun logout(): Call<ResponseBody>
}
