package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class SettingsViewModel : ViewModel() {
    private val apiService = ApiClient.instance

    private val _settings = mutableStateOf<SettingsResponse?>(null)
    val settings: State<SettingsResponse?> = _settings

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchSettings() {
        _isLoading.value = true
        apiService.getSettings().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        _settings.value = Gson().fromJson(jsonString, SettingsResponse::class.java)
                    } catch (e: Exception) {
                        _error.value = "Failed to parse settings"
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Failed to fetch settings"
                    _error.value = if (errorBody.contains("<!DOCTYPE html>")) "Server error fetching settings." else errorBody
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun saveSettings(theme: String, language: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        val settingsData = mapOf("theme" to theme, "language" to language)
        val body = Gson().toJson(settingsData).toRequestBody("application/json".toMediaTypeOrNull())
        apiService.saveSettings(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        val result = Gson().fromJson(jsonString, SaveSettingsResponse::class.java)
                        _settings.value = result?.settings
                        onComplete(true)
                    } catch (e: Exception) {
                        _error.value = "Failed to parse save settings response"
                        onComplete(false)
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Failed to save settings"
                    _error.value = if (errorBody.contains("<!DOCTYPE html>")) "Server error saving settings." else errorBody
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
                onComplete(false)
            }
        })
    }
}
