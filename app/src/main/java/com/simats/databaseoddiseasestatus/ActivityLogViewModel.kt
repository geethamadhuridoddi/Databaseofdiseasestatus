package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityLogViewModel : ViewModel() {
    private val apiService = ApiClient.instance

    private val _logs = mutableStateOf<List<ActivityLogItem>>(emptyList())
    val logs: State<List<ActivityLogItem>> = _logs

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchActivityLog() {
        _isLoading.value = true
        apiService.getActivityLog().enqueue(object : Callback<List<ActivityLogItem>> {
            override fun onResponse(call: Call<List<ActivityLogItem>>, response: Response<List<ActivityLogItem>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _logs.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to fetch activity logs"
                }
            }

            override fun onFailure(call: Call<List<ActivityLogItem>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }
}
