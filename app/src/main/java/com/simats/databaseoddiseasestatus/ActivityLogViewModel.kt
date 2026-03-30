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
        _error.value = null
        try {
            apiService.getActivityLog().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    _isLoading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val type = object : TypeToken<List<ActivityLogItem>>() {}.type
                            val logList: List<ActivityLogItem> = Gson().fromJson(jsonString, type)
                            Log.d("ActivityLogVM", "Fetched ${logList.size} logs")
                            _logs.value = logList
                        } catch (e: Exception) {
                            Log.e("ActivityLogVM", "Parsing error", e)
                            _error.value = "Failed to parse activity logs"
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Failed to fetch activity logs"
                        Log.e("ActivityLogVM", "Error response: $errorBody")
                        _error.value = "Server error: $errorBody"
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isLoading.value = false
                    Log.e("ActivityLogVM", "Network failure", t)
                }
            })
        } catch (e: Exception) {
            _isLoading.value = false
            Log.e("ActivityLogVM", "Launch error fetching logs", e)
            _error.value = "Launch error: ${e.localizedMessage}"
        }
    }
}
