package com.simats.databaseoddiseasestatus

import android.util.Log
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
        try {
            apiService.getReportSummary(userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    _isLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            _summary.value = Gson().fromJson(jsonString, ReportSummaryResponse::class.java)
                        } catch (e: Exception) {
                            Log.e("ReportViewModel", "Failed to parse summary", e)
                        }
                    } else {
                        Log.e("ReportViewModel", "Server error fetching summary: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("ReportViewModel", "Network error fetching summary", t)
                }
            })
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("ReportViewModel", "Launch error fetching summary", e)
        }
    }

    fun fetchDashboardStats(userId: Int? = null) {
        try {
            apiService.getDashboardStats(userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val stats = Gson().fromJson(jsonString, DashboardResponse::class.java)
                            
                            // Fallback calculation if specific case counts are 0 but status_summary has data
                            val finalStats = if (stats?.totalCases == 0 && stats.statusSummary != null) {
                                val active = stats.statusSummary.find { it.status.equals("Active", true) }?.count ?: 0
                                val recovering = stats.statusSummary.find { it.status.equals("Recovering", true) }?.count ?: 0
                                val critical = stats.statusSummary.find { it.status.equals("Critical", true) }?.count ?: 0
                                val total = stats.statusSummary.sumOf { it.count }
                                
                                stats.copy(
                                    totalCases = total,
                                    activeCases = active,
                                    recoveringCases = recovering,
                                    criticalCases = critical
                                )
                            } else {
                                stats
                            }
                            
                            _dashboardStats.value = finalStats
                            _totalPatients.value = finalStats?.totalCases ?: 0
                        } catch (e: Exception) {}
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Launch error fetching stats", e)
        }
    }

    fun fetchPatientReport(userId: Int? = null) {
        _isLoading.value = true
        try {
            apiService.getPatientReport(userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    _isLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val type = object : TypeToken<List<PatientReportItem>>() {}.type
                            _patientReport.value = Gson().fromJson(jsonString, type)
                        } catch (e: Exception) {
                            Log.e("ReportViewModel", "Failed to parse patient report", e)
                        }
                    } else {
                        Log.e("ReportViewModel", "Server error fetching patient report: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("ReportViewModel", "Network error fetching patient report", t)
                }
            })
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("ReportViewModel", "Launch error fetching patient report", e)
        }
    }

    fun fetchDiseaseAnalytics(userId: Int? = null) {
        _isLoading.value = true
        try {
            apiService.getDiseaseAnalytics(userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    _isLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            _analytics.value = Gson().fromJson(jsonString, DiseaseAnalyticsResponse::class.java)
                        } catch (e: Exception) {
                            Log.e("ReportViewModel", "Failed to parse analytics", e)
                        }
                    } else {
                        Log.e("ReportViewModel", "Server error fetching analytics: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("ReportViewModel", "Network error fetching analytics", t)
                }
            })
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("ReportViewModel", "Launch error fetching analytics", e)
        }
    }

    fun downloadReport(format: String, userId: Int? = null, onComplete: (Boolean) -> Unit) {
        try {
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
        } catch (e: Exception) {
            Log.e("ReportViewModel", "Launch error downloading report", e)
            onComplete(false)
        }
    }
}
