package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportViewModel : ViewModel() {
    private val apiService = ApiClient.instance

    private val _summary = mutableStateOf<ReportSummaryResponse?>(null)
    val summary: State<ReportSummaryResponse?> = _summary

    private val _totalPatients = mutableStateOf(0)
    val totalPatients: State<Int> = _totalPatients

    private val _patientReport = mutableStateOf<List<PatientReportItem>>(emptyList())
    val patientReport: State<List<PatientReportItem>> = _patientReport

    private val _analytics = mutableStateOf<DiseaseAnalyticsResponse?>(null)
    val analytics: State<DiseaseAnalyticsResponse?> = _analytics

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchReportData() {
        fetchReportSummary()
        fetchDashboardStats()
    }

    fun fetchReportSummary() {
        _isLoading.value = true
        apiService.getReportSummary().enqueue(object : Callback<ReportSummaryResponse> {
            override fun onResponse(call: Call<ReportSummaryResponse>, response: Response<ReportSummaryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _summary.value = response.body()
                } else {
                    _error.value = "Failed to fetch summary"
                }
            }

            override fun onFailure(call: Call<ReportSummaryResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun fetchDashboardStats() {
        apiService.getDashboardStats().enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                if (response.isSuccessful) {
                    _totalPatients.value = response.body()?.totalPatients ?: 0
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {}
        })
    }

    fun fetchPatientReport() {
        _isLoading.value = true
        apiService.getPatientReport().enqueue(object : Callback<List<PatientReportItem>> {
            override fun onResponse(call: Call<List<PatientReportItem>>, response: Response<List<PatientReportItem>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _patientReport.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to fetch patient report"
                }
            }

            override fun onFailure(call: Call<List<PatientReportItem>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun fetchDiseaseAnalytics() {
        _isLoading.value = true
        apiService.getDiseaseAnalytics().enqueue(object : Callback<DiseaseAnalyticsResponse> {
            override fun onResponse(call: Call<DiseaseAnalyticsResponse>, response: Response<DiseaseAnalyticsResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _analytics.value = response.body()
                } else {
                    _error.value = "Failed to fetch analytics"
                }
            }

            override fun onFailure(call: Call<DiseaseAnalyticsResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun downloadReport(format: String, onComplete: (Boolean) -> Unit) {
        apiService.downloadReport(format).enqueue(object : Callback<ResponseBody> {
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
