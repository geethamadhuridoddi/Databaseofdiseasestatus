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

    fun registerUser(userData: Map<String, Any>) {
        _registrationState.value = RegistrationResult.Loading
        viewModelScope.launch {
            ApiClient.instance.registerUser(userData).enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _registrationState.value = RegistrationResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Registration failed. Please try again."
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _registrationState.value = RegistrationResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _registrationState.value = RegistrationResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun loginUser(credentials: Map<String, String>) {
        _loginState.value = LoginResult.Loading
        viewModelScope.launch {
            ApiClient.instance.loginUser(credentials).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _loginState.value = LoginResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Login failed. Please check your credentials."
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _loginState.value = LoginResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _loginState.value = LoginResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun forgotPassword(emailData: Map<String, String>) {
        _forgotPasswordState.value = ForgotPasswordResult.Loading
        viewModelScope.launch {
            ApiClient.instance.forgotPassword(emailData).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _forgotPasswordState.value = ForgotPasswordResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to send reset code. Please check your email."
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _forgotPasswordState.value = ForgotPasswordResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _forgotPasswordState.value = ForgotPasswordResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun verifyForgotOtp(otpData: Map<String, String>) {
        _verifyOtpState.value = VerifyOtpResult.Loading
        viewModelScope.launch {
            ApiClient.instance.verifyForgotOtp(otpData).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _verifyOtpState.value = VerifyOtpResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "OTP verification failed."
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _verifyOtpState.value = VerifyOtpResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _verifyOtpState.value = VerifyOtpResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun resetPassword(resetData: Map<String, String>) {
        _resetPasswordState.value = ResetPasswordResult.Loading
        viewModelScope.launch {
            ApiClient.instance.resetPassword(resetData).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _resetPasswordState.value = ResetPasswordResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Password reset failed."
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _resetPasswordState.value = ResetPasswordResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _resetPasswordState.value = ResetPasswordResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun fetchDashboardStats() {
        _dashboardState.value = DashboardResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getDashboardStats().enqueue(object : Callback<DashboardResponse> {
                override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _dashboardState.value = DashboardResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to fetch dashboard stats."
                        Log.e("AuthViewModel", "Error: $errorMsg")
                        _dashboardState.value = DashboardResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                    Log.e("AuthViewModel", "Failure: ${t.message}")
                    _dashboardState.value = DashboardResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

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
