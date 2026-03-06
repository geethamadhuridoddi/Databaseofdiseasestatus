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

class PatientViewModel : ViewModel() {

    private val _patientsState = MutableStateFlow<PatientsResult>(PatientsResult.Idle)
    val patientsState: StateFlow<PatientsResult> = _patientsState

    private val _addPatientState = MutableStateFlow<AddPatientResult>(AddPatientResult.Idle)
    val addPatientState: StateFlow<AddPatientResult> = _addPatientState

    private val _updatePatientState = MutableStateFlow<UpdatePatientResult>(UpdatePatientResult.Idle)
    val updatePatientState: StateFlow<UpdatePatientResult> = _updatePatientState

    private val _deletePatientState = MutableStateFlow<DeletePatientResult>(DeletePatientResult.Idle)
    val deletePatientState: StateFlow<DeletePatientResult> = _deletePatientState

    fun fetchPatients() {
        _patientsState.value = PatientsResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getPatients().enqueue(object : Callback<List<Patient>> {
                override fun onResponse(call: Call<List<Patient>>, response: Response<List<Patient>>) {
                    if (response.isSuccessful && response.body() != null) {
                        _patientsState.value = PatientsResult.Success(response.body()!!)
                        globalPatients.clear()
                        globalPatients.addAll(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to fetch patients."
                        Log.e("PatientViewModel", "Error: $errorMsg")
                        _patientsState.value = PatientsResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<List<Patient>>, t: Throwable) {
                    Log.e("PatientViewModel", "Failure: ${t.message}")
                    _patientsState.value = PatientsResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun addPatient(patientData: Map<String, Any>) {
        _addPatientState.value = AddPatientResult.Loading
        viewModelScope.launch {
            ApiClient.instance.addPatient(patientData).enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        _addPatientState.value = AddPatientResult.Success(response.body()!!)
                        fetchPatients()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to add patient."
                        _addPatientState.value = AddPatientResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    _addPatientState.value = AddPatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun updatePatient(patientId: String, patientData: Map<String, Any>) {
        _updatePatientState.value = UpdatePatientResult.Loading
        viewModelScope.launch {
            val id = patientId.toIntOrNull() ?: 0
            ApiClient.instance.updatePatient(id, patientData).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        _updatePatientState.value = UpdatePatientResult.Success
                        fetchPatients()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to update patient."
                        _updatePatientState.value = UpdatePatientResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    _updatePatientState.value = UpdatePatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun deletePatient(patientId: String) {
        _deletePatientState.value = DeletePatientResult.Loading
        viewModelScope.launch {
            val id = patientId.toIntOrNull() ?: 0
            ApiClient.instance.deletePatient(id).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        _deletePatientState.value = DeletePatientResult.Success
                        fetchPatients()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to delete patient."
                        _deletePatientState.value = DeletePatientResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    _deletePatientState.value = DeletePatientResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun resetAddPatientState() { _addPatientState.value = AddPatientResult.Idle }
    fun resetUpdatePatientState() { _updatePatientState.value = UpdatePatientResult.Idle }
    fun resetDeletePatientState() { _deletePatientState.value = DeletePatientResult.Idle }
}

sealed class PatientsResult {
    object Idle : PatientsResult()
    object Loading : PatientsResult()
    data class Success(val patients: List<Patient>) : PatientsResult()
    data class Error(val message: String) : PatientsResult()
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
