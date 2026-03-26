package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReportViewModel : ViewModel() {
    private val apiService = ApiClient.instance

    private val _summary = mutableStateOf<ReportSummaryResponse?>(null)
    val summary: State<ReportSummaryResponse?> = _summary

    private val _totalPatients = mutableStateOf(0)
    val totalPatients: State<Int> = _totalPatients

    private val _dashboardStats = mutableStateOf<DashboardResponse?>(null)
    val dashboardStats: State<DashboardResponse?> = _dashboardStats

    private val _patientReport = mutableStateOf<List<PatientReportItem>>(emptyList())
    val patientReport: State<List<PatientReportItem>> = _patientReport

    private val _analytics = mutableStateOf<DiseaseAnalyticsResponse?>(null)
    val analytics: State<DiseaseAnalyticsResponse?> = _analytics

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchReportData(userId: Int? = null) {
        fetchReportSummary(userId)
        fetchDashboardStats(userId)
    }

    fun fetchReportSummary(userId: Int? = null) {
        _isLoading.value = true
        apiService.getReportSummary(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        _summary.value = Gson().fromJson(jsonString, ReportSummaryResponse::class.java)
                    } catch (e: Exception) {
                        _error.value = "Failed to parse summary"
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Failed to fetch summary"
                    _error.value = if (errorBody.contains("<!DOCTYPE html>")) "Server error fetching summary." else errorBody
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun fetchDashboardStats(userId: Int? = null) {
        apiService.getDashboardStats(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        val stats = Gson().fromJson(jsonString, DashboardResponse::class.java)
                        _dashboardStats.value = stats
                        _totalPatients.value = stats?.totalCases ?: 0
                    } catch (e: Exception) {}
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    fun fetchPatientReport(userId: Int? = null) {
        _isLoading.value = true
        apiService.getPatientReport(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        val type = object : TypeToken<List<PatientReportItem>>() {}.type
                        _patientReport.value = Gson().fromJson(jsonString, type)
                    } catch (e: Exception) {
                        _error.value = "Failed to parse patient report"
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Failed to fetch patient report"
                    _error.value = if (errorBody.contains("<!DOCTYPE html>")) "Server error fetching report." else errorBody
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun fetchDiseaseAnalytics(userId: Int? = null) {
        _isLoading.value = true
        apiService.getDiseaseAnalytics(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        _analytics.value = Gson().fromJson(jsonString, DiseaseAnalyticsResponse::class.java)
                    } catch (e: Exception) {
                        _error.value = "Failed to parse analytics"
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Failed to fetch analytics"
                    _error.value = if (errorBody.contains("<!DOCTYPE html>")) "Server error fetching analytics." else errorBody
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun downloadReport(format: String, userId: Int? = null, onComplete: (Boolean) -> Unit) {
        apiService.downloadReport(format, userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(false)
            }
        })
    }
}
