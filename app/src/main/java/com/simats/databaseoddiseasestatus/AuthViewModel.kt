package com.simats.databaseoddiseasestatus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class AuthViewModel : ViewModel() {

    private val _registrationState = MutableStateFlow<RegistrationResult>(RegistrationResult.Idle)
    val registrationState: StateFlow<RegistrationResult> = _registrationState

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState: StateFlow<LoginResult> = _loginState

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordResult>(ForgotPasswordResult.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordResult> = _forgotPasswordState

    private val _verifyOtpState = MutableStateFlow<VerifyOtpResult>(VerifyOtpResult.Idle)
    val verifyOtpState: StateFlow<VerifyOtpResult> = _verifyOtpState

    private val _resetPasswordState = MutableStateFlow<ResetPasswordResult>(ResetPasswordResult.Idle)
    val resetPasswordState: StateFlow<ResetPasswordResult> = _resetPasswordState

    private val _dashboardState = MutableStateFlow<DashboardResult>(DashboardResult.Idle)
    val dashboardState: StateFlow<DashboardResult> = _dashboardState

    private fun handleApiError(errorBody: String?): String {
        if (errorBody == null) return "An unknown error occurred"
        return if (errorBody.contains("<!DOCTYPE html>", ignoreCase = true) || errorBody.contains("<html>", ignoreCase = true)) {
            "Server error: Requested page not found or server misconfigured. Please contact support."
        } else {
            errorBody
        }
    }

    fun registerUser(userData: Map<String, Any>) {
        _registrationState.value = RegistrationResult.Loading
        viewModelScope.launch {
            try {
                val body = Gson().toJson(userData).toRequestBody("application/json".toMediaTypeOrNull())
                ApiClient.instance.registerUser(body).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val jsonString = response.body()!!.string()
                                val registrationResponse = Gson().fromJson(jsonString, RegistrationResponse::class.java)
                                if (registrationResponse != null) {
                                    _registrationState.value = RegistrationResult.Success(registrationResponse)
                                } else {
                                    _registrationState.value = RegistrationResult.Error("Empty response from server")
                                }
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "Parsing error", e)
                                _registrationState.value = RegistrationResult.Error("Failed to process registration data")
                            }
                        } else {
                            val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                            val errorMsg = handleApiError(errorBody)
                            Log.e("AuthViewModel", "Error: $errorMsg")
                            _registrationState.value = RegistrationResult.Error(errorMsg)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("AuthViewModel", "Failure: ${t.message}")
                        _registrationState.value = RegistrationResult.Error("Network error: ${t.localizedMessage}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration Launch Error", e)
                _registrationState.value = RegistrationResult.Error("Could not start registration: ${e.localizedMessage}")
            }
        }
    }

    fun loginUser(credentials: Map<String, String>) {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val body = Gson().toJson(credentials).toRequestBody("application/json".toMediaTypeOrNull())
                ApiClient.instance.loginUser(body).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val jsonString = response.body()!!.string()
                                val loginResponse = Gson().fromJson(jsonString, LoginResponse::class.java)
                                if (loginResponse != null) {
                                    _loginState.value = LoginResult.Success(loginResponse)
                                } else {
                                    _loginState.value = LoginResult.Error("Invalid login response")
                                }
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "Login Parsing error", e)
                                _loginState.value = LoginResult.Error("Failed to process login data")
                            }
                        } else {
                            val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                            val errorMsg = handleApiError(errorBody)
                            Log.e("AuthViewModel", "Login API Error: $errorMsg")
                            _loginState.value = LoginResult.Error(errorMsg)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("AuthViewModel", "Failure: ${t.message}")
                        _loginState.value = LoginResult.Error("Network error: ${t.localizedMessage}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login Launch Error", e)
                _loginState.value = LoginResult.Error("Could not start login: ${e.localizedMessage}")
            }
        }
    }

    fun forgotPassword(emailData: Map<String, String>) {
        _forgotPasswordState.value = ForgotPasswordResult.Loading
        viewModelScope.launch {
            val body = Gson().toJson(emailData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.forgotPassword(body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val genericResponse = Gson().fromJson(jsonString, GenericResponse::class.java)
                            _forgotPasswordState.value = ForgotPasswordResult.Success(genericResponse)
                        } catch (e: Exception) {
                            _forgotPasswordState.value = ForgotPasswordResult.Error("Failed to process response")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _forgotPasswordState.value = ForgotPasswordResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _forgotPasswordState.value = ForgotPasswordResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun verifyForgotOtp(otpData: Map<String, String>) {
        _verifyOtpState.value = VerifyOtpResult.Loading
        viewModelScope.launch {
            val body = Gson().toJson(otpData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.verifyForgotOtp(body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val genericResponse = Gson().fromJson(jsonString, GenericResponse::class.java)
                            _verifyOtpState.value = VerifyOtpResult.Success(genericResponse)
                        } catch (e: Exception) {
                            _verifyOtpState.value = VerifyOtpResult.Error("Failed to parse OTP response")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _verifyOtpState.value = VerifyOtpResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _verifyOtpState.value = VerifyOtpResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun resetPassword(resetData: Map<String, String>) {
        _resetPasswordState.value = ResetPasswordResult.Loading
        viewModelScope.launch {
            val body = Gson().toJson(resetData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.resetPassword(body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val genericResponse = Gson().fromJson(jsonString, GenericResponse::class.java)
                            _resetPasswordState.value = ResetPasswordResult.Success(genericResponse)
                        } catch (e: Exception) {
                            _resetPasswordState.value = ResetPasswordResult.Error("Failed to parse response")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _resetPasswordState.value = ResetPasswordResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _resetPasswordState.value = ResetPasswordResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun fetchDashboardStats(userId: Int?) {
        _dashboardState.value = DashboardResult.Loading
        viewModelScope.launch {
            try {
                ApiClient.instance.getDashboardStats(userId).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val jsonString = response.body()!!.string()
                                val dashboardResponse = Gson().fromJson(jsonString, DashboardResponse::class.java)
                                if (dashboardResponse != null) {
                                    // Fallback calculation if specific case counts are 0 but status_summary has data
                                    val finalResponse = if (dashboardResponse.totalCases == 0 && dashboardResponse.statusSummary != null) {
                                        val active = dashboardResponse.statusSummary.find { it.status.equals("Active", true) }?.count ?: 0
                                        val recovering = dashboardResponse.statusSummary.find { it.status.equals("Recovering", true) }?.count ?: 0
                                        val critical = dashboardResponse.statusSummary.find { it.status.equals("Critical", true) }?.count ?: 0
                                        val total = dashboardResponse.statusSummary.sumOf { it.count }
                                        
                                        dashboardResponse.copy(
                                            totalCases = total,
                                            activeCases = active,
                                            recoveringCases = recovering,
                                            criticalCases = critical
                                        )
                                    } else {
                                        dashboardResponse
                                    }
                                    _dashboardState.value = DashboardResult.Success(finalResponse)
                                } else {
                                    _dashboardState.value = DashboardResult.Success(defaultDashboard())
                                }
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "Dashboard Parsing error", e)
                                _dashboardState.value = DashboardResult.Success(defaultDashboard())
                            }
                        } else {
                            Log.e("AuthViewModel", "Dashboard API Error: ${response.code()}")
                            _dashboardState.value = DashboardResult.Success(defaultDashboard())
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("AuthViewModel", "Network error loading dashboard", t)
                        _dashboardState.value = DashboardResult.Success(defaultDashboard())
                    }
                })
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Dashboard Launch Error", e)
                _dashboardState.value = DashboardResult.Success(defaultDashboard())
            }
        }
    }

    private fun defaultDashboard() = DashboardResponse(
        totalPatients = 0, totalDiseases = 0, totalCases = 0,
        activeCases = 0, recoveringCases = 0, criticalCases = 0
    )

    fun resetRegistrationState() {
        _registrationState.value = RegistrationResult.Idle
    }

    fun resetLoginState() {
        _loginState.value = LoginResult.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordResult.Idle
    }

    fun resetVerifyOtpState() {
        _verifyOtpState.value = VerifyOtpResult.Idle
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = ResetPasswordResult.Idle
    }
}

sealed class RegistrationResult {
    object Idle : RegistrationResult()
    object Loading : RegistrationResult()
    data class Success(val response: RegistrationResponse) : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val response: LoginResponse) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

sealed class ForgotPasswordResult {
    object Idle : ForgotPasswordResult()
    object Loading : ForgotPasswordResult()
    data class Success(val response: GenericResponse) : ForgotPasswordResult()
    data class Error(val message: String) : ForgotPasswordResult()
}

sealed class VerifyOtpResult {
    object Idle : VerifyOtpResult()
    object Loading : VerifyOtpResult()
    data class Success(val response: GenericResponse) : VerifyOtpResult()
    data class Error(val message: String) : VerifyOtpResult()
}

sealed class ResetPasswordResult {
    object Idle : ResetPasswordResult()
    object Loading : ResetPasswordResult()
    data class Success(val response: GenericResponse) : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
}

sealed class DashboardResult {
    object Idle : DashboardResult()
    object Loading : DashboardResult()
    data class Success(val response: DashboardResponse) : DashboardResult()
    data class Error(val message: String) : DashboardResult()
}
