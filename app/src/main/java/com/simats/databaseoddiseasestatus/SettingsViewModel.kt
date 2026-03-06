package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        apiService.getSettings().enqueue(object : Callback<SettingsResponse> {
            override fun onResponse(call: Call<SettingsResponse>, response: Response<SettingsResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _settings.value = response.body()
                } else {
                    _error.value = "Failed to fetch settings"
                }
            }

            override fun onFailure(call: Call<SettingsResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun saveSettings(theme: String, language: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        val settingsData = mapOf("theme" to theme, "language" to language)
        apiService.saveSettings(settingsData).enqueue(object : Callback<SaveSettingsResponse> {
            override fun onResponse(call: Call<SaveSettingsResponse>, response: Response<SaveSettingsResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _settings.value = response.body()?.settings
                    onComplete(true)
                } else {
                    _error.value = "Failed to save settings"
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<SaveSettingsResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
                onComplete(false)
            }
        })
    }
}
