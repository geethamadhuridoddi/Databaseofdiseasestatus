package com.simats.databaseoddiseasestatus

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class ProfileViewModel : ViewModel() {
    private val apiService = ApiClient.instance

    private val _profile = mutableStateOf<ProfileResponse?>(null)
    val profile: State<ProfileResponse?> = _profile

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchProfile(email: String, userId: Int? = null) {
        if (email.isBlank() && userId == null) {
            _error.value = "User identification is missing"
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        apiService.getProfile(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        _profile.value = Gson().fromJson(jsonString, ProfileResponse::class.java)
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Parsing error", e)
                    }
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                    Log.e("ProfileViewModel", "Fetch Profile Error: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                Log.e("ProfileViewModel", "Fetch Profile Failure", t)
            }
        })
    }

    fun updateProfile(userId: Int?, name: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        _error.value = null
        
        val profileData = mutableMapOf<String, Any>(
            "name" to name
        )
        if (userId != null) {
            profileData["user_id"] = userId
        }
        val body = Gson().toJson(profileData).toRequestBody("application/json".toMediaTypeOrNull())
        apiService.updateProfile(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    // Update successful, fetch profile with the NEW email
                    fetchProfile("", userId)
                    onComplete(true)
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                    Log.e("ProfileViewModel", "Update Profile Error: ${response.code()} - $errorBody")
                    
                    val errorMessage = try {
                        val gson = Gson()
                        val map = gson.fromJson(errorBody, Map::class.java)
                        map["error"]?.toString() ?: map["message"]?.toString() ?: "Update failed"
                    } catch (e: Exception) {
                        "Update failed (${response.code()})"
                    }
                    
                    _error.value = errorMessage
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _isLoading.value = false
                Log.e("ProfileViewModel", "Update Profile Failure", t)
                _error.value = "Network error: ${t.localizedMessage}"
                onComplete(false)
            }
        })
    }

    fun logout(onComplete: (Boolean) -> Unit) {
        apiService.logout().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onComplete(response.isSuccessful)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onComplete(false)
            }
        })
    }
}
