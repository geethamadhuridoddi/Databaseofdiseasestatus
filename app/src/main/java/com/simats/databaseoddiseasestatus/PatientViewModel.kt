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
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*

class PatientViewModel : ViewModel() {

    private val _patientsState = MutableStateFlow<PatientsResult>(PatientsResult.Idle)
    val patientsState: StateFlow<PatientsResult> = _patientsState

    private val _addPatientState = MutableStateFlow<AddPatientResult>(AddPatientResult.Idle)
    val addPatientState: StateFlow<AddPatientResult> = _addPatientState

    private val _updatePatientState = MutableStateFlow<UpdatePatientResult>(UpdatePatientResult.Idle)
    val updatePatientState: StateFlow<UpdatePatientResult> = _updatePatientState

    private val _deletePatientState = MutableStateFlow<DeletePatientResult>(DeletePatientResult.Idle)
    val deletePatientState: StateFlow<DeletePatientResult> = _deletePatientState

    private val _singlePatientState = MutableStateFlow<SinglePatientResult>(SinglePatientResult.Idle)
    val singlePatientState: StateFlow<SinglePatientResult> = _singlePatientState

    private val _historyState = MutableStateFlow<HistoryResult>(HistoryResult.Idle)
    val historyState: StateFlow<HistoryResult> = _historyState

    private fun handleApiError(errorBody: String?): String {
        if (errorBody == null) return "An unknown error occurred"
        return if (errorBody.contains("<!DOCTYPE html>", ignoreCase = true) || errorBody.contains("<html>", ignoreCase = true)) {
            "Server configuration error. The requested resource might not exist or the URL is incorrect."
        } else {
            errorBody
        }
    }

    private fun addLocalNotification(message: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val newNotification = Notification(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            message = message,
            isRead = false,
            createdAt = currentDate
        )
        globalNotifications.add(newNotification)
    }

    fun fetchPatients(status: String? = null, userId: Int? = null) {
        _patientsState.value = PatientsResult.Loading
        viewModelScope.launch {
            try {
                val call = if (!status.isNullOrBlank() && status != "All") {
                    ApiClient.instance.getCasesByStatus(status.lowercase(), userId = userId)
                } else {
                    ApiClient.instance.getPatients(userId = userId)
                }

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        try {
                            if (response.isSuccessful && response.body() != null) {
                                val jsonString = response.body()!!.string()
                                
                                val patients: List<Patient> = if (!status.isNullOrBlank() && status != "All") {
                                    val caseType = object : TypeToken<List<CaseRecord>>() {}.type
                                    val cases: List<CaseRecord> = Gson().fromJson(jsonString, caseType)
                                    cases?.map { it.toPatient() } ?: emptyList()
                                } else {
                                    val patientType = object : TypeToken<List<Patient>>() {}.type
                                    Gson().fromJson(jsonString, patientType) ?: emptyList()
                                }
                                
                                if (patients.isNotEmpty() || jsonString == "[]") {
                                    _patientsState.value = PatientsResult.Success(patients)
                                    // Update global cache only for "All" view to avoid mixing filtered/unfiltered
                                    if (status == null || status == "All") {
                                        globalPatients.clear()
                                        globalPatients.addAll(patients)
                                    }
                                } else {
                                    _patientsState.value = PatientsResult.Error("No patient data found")
                                }
                            } else {
                                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                                val errorMsg = handleApiError(errorBody)
                                Log.e("PatientViewModel", "Error fetching patients: $errorMsg")
                                _patientsState.value = PatientsResult.Error(errorMsg)
                            }
                        } catch (e: Exception) {
                            Log.e("PatientViewModel", "Error parsing patients list", e)
                            _patientsState.value = PatientsResult.Error("Data processing error: ${e.localizedMessage}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("PatientViewModel", "Failure: ${t.message}")
                        _patientsState.value = PatientsResult.Error("Network error: ${t.localizedMessage}")
                    }
                })
            } catch (e: Exception) {
                Log.e("PatientViewModel", "Error launching API call", e)
                _patientsState.value = PatientsResult.Error("Request error: ${e.localizedMessage}")
            }
        }
    }

    fun addPatient(patientData: Map<String, Any>) {
        _addPatientState.value = AddPatientResult.Loading
        viewModelScope.launch {
            val body = Gson().toJson(patientData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.addPatient(body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val result = Gson().fromJson(jsonString, RegistrationResponse::class.java)
                            if (result != null) {
                                _addPatientState.value = AddPatientResult.Success(result)
                                val patientName = patientData["name"]?.toString() ?: "New Patient"
                                addLocalNotification("New patient added: $patientName")
                                val userId = patientData["user_id"]?.toString()?.toIntOrNull()
                                fetchPatients(userId = userId)
                            } else {
                                _addPatientState.value = AddPatientResult.Error("Server returned empty confirmation")
                            }
                        } catch (e: Exception) {
                            Log.e("PatientViewModel", "Add Patient Parsing Error", e)
                            _addPatientState.value = AddPatientResult.Error("Failed to process server confirmation")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        _addPatientState.value = AddPatientResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _addPatientState.value = AddPatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun updatePatient(patientId: String, patientData: Map<String, Any>, userId: Int? = null) {
        _updatePatientState.value = UpdatePatientResult.Loading
        viewModelScope.launch {
            val body = Gson().toJson(patientData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.updatePatient(patientId, body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _updatePatientState.value = UpdatePatientResult.Success
                        val patientName = patientData["name"]?.toString() ?: "Patient"
                        addLocalNotification("Patient profile updated: $patientName")
                        fetchPatients()
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        Log.e("PatientViewModel", "Update Error: $errorMsg")
                        _updatePatientState.value = UpdatePatientResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _updatePatientState.value = UpdatePatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun deletePatient(patientId: String, userId: Int? = null) {
        _deletePatientState.value = DeletePatientResult.Loading
        viewModelScope.launch {
            val patientName = globalPatients.find { it.id.toString() == patientId }?.name ?: "Patient"
            
            ApiClient.instance.deletePatient(patientId, userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _deletePatientState.value = DeletePatientResult.Success
                        addLocalNotification("Patient deleted: $patientName")
                        globalPatients.removeAll { it.id.toString() == patientId }
                        fetchPatients(userId = userId)
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        _deletePatientState.value = DeletePatientResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _deletePatientState.value = DeletePatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun fetchHistory(patientId: String, userId: Int? = null) {
        _historyState.value = HistoryResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getPatientHistory(patientId, userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val type = object : TypeToken<List<Disease>>() {}.type
                            val history: List<Disease> = Gson().fromJson(jsonString, type)
                            if (history != null) {
                                _historyState.value = HistoryResult.Success(history)
                            } else {
                                _historyState.value = HistoryResult.Error("No history found")
                            }
                        } catch (e: Exception) {
                            Log.e("PatientViewModel", "History parsing error", e)
                            _historyState.value = HistoryResult.Error("Failed to parse patient history")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        _historyState.value = HistoryResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _historyState.value = HistoryResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun fetchPatientDetails(patientId: String, userId: Int? = null) {
        _singlePatientState.value = SinglePatientResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getPatient(patientId, userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val patient = Gson().fromJson(jsonString, Patient::class.java)
                            if (patient != null) {
                                _singlePatientState.value = SinglePatientResult.Success(patient)
                                val index = globalPatients.indexOfFirst { it.id == patient.id }
                                if (index != -1) {
                                    val oldPatient = globalPatients[index]
                                    globalPatients[index] = patient.copy(
                                        diseases = patient.diseases ?: oldPatient.diseases
                                    )
                                }
                            } else {
                                _singlePatientState.value = SinglePatientResult.Error("Patient data missing from response")
                            }
                        } catch (e: Exception) {
                            Log.e("PatientViewModel", "Details parsing error", e)
                            _singlePatientState.value = SinglePatientResult.Error("Failed to parse patient details")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        val errorMsg = handleApiError(errorBody)
                        _singlePatientState.value = SinglePatientResult.Error("Could not load details: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _singlePatientState.value = SinglePatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun resetAddPatientState() { _addPatientState.value = AddPatientResult.Idle }
    fun resetUpdatePatientState() { _updatePatientState.value = UpdatePatientResult.Idle }
    fun resetDeletePatientState() { _deletePatientState.value = DeletePatientResult.Idle }
    fun resetSinglePatientState() { _singlePatientState.value = SinglePatientResult.Idle }
}

sealed class PatientsResult {
    object Idle : PatientsResult()
    object Loading : PatientsResult()
    data class Success(val patients: List<Patient>) : PatientsResult()
    data class Error(val message: String) : PatientsResult()
}

sealed class SinglePatientResult {
    object Idle : SinglePatientResult()
    object Loading : SinglePatientResult()
    data class Success(val patient: Patient) : SinglePatientResult()
    data class Error(val message: String) : SinglePatientResult()
}

sealed class AddPatientResult {
    object Idle : AddPatientResult()
    object Loading : AddPatientResult()
    data class Success(val response: RegistrationResponse) : AddPatientResult()
    data class Error(val message: String) : AddPatientResult()
}

sealed class UpdatePatientResult {
    object Idle : UpdatePatientResult()
    object Loading : UpdatePatientResult()
    object Success : UpdatePatientResult()
    data class Error(val message: String) : UpdatePatientResult()
}

sealed class DeletePatientResult {
    object Idle : DeletePatientResult()
    object Loading : DeletePatientResult()
    object Success : DeletePatientResult()
    data class Error(val message: String) : DeletePatientResult()
}

sealed class HistoryResult {
    object Idle : HistoryResult()
    object Loading : HistoryResult()
    data class Success(val history: List<Disease>) : HistoryResult()
    data class Error(val message: String) : HistoryResult()
}
