package com.simats.databaseoddiseasestatus

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    private val apiService = ApiClient.instance

    private val _profile = mutableStateOf<ProfileResponse?>(null)
    val profile: State<ProfileResponse?> = _profile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchProfile() {
        _isLoading.value = true
        apiService.getProfile().enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _profile.value = response.body()
                } else {
                    _error.value = "Failed to fetch profile"
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
            }
        })
    }

    fun updateProfile(name: String, phone: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        val profileData = mapOf("name" to name, "phone" to phone)
        apiService.updateProfile(profileData).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    fetchProfile() // Refresh profile after update
                    onComplete(true)
                } else {
                    _error.value = "Failed to update profile"
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error: ${t.message}"
                onComplete(false)
            }
        })
    }

    fun logout(onComplete: (Boolean) -> Unit) {
        apiService.logout().enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                onComplete(false)
            }
        })
    }
}
