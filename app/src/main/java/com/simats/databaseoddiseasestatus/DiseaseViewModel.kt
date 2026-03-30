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

class DiseaseViewModel : ViewModel() {

    private val _diseasesState = MutableStateFlow<DiseasesResult>(DiseasesResult.Idle)
    val diseasesState: StateFlow<DiseasesResult> = _diseasesState

    private val _addDiseaseState = MutableStateFlow<AddDiseaseResult>(AddDiseaseResult.Idle)
    val addDiseaseState: StateFlow<AddDiseaseResult> = _addDiseaseState

    private val _updateDiseaseState = MutableStateFlow<UpdateDiseaseResult>(UpdateDiseaseResult.Idle)
    val updateDiseaseState: StateFlow<UpdateDiseaseResult> = _updateDiseaseState

    private val _deleteDiseaseState = MutableStateFlow<DeleteDiseaseResult>(DeleteDiseaseResult.Idle)
    val deleteDiseaseState: StateFlow<DeleteDiseaseResult> = _deleteDiseaseState

    private val _singleRecordState = MutableStateFlow<SingleRecordResult>(SingleRecordResult.Idle)
    val singleRecordState: StateFlow<SingleRecordResult> = _singleRecordState

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

    fun fetchDiseases(userId: Int? = null) {
        _diseasesState.value = DiseasesResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getDiseases(userId = userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val type = object : TypeToken<List<DiseaseCatalogItem>>() {}.type
                            val diseases: List<DiseaseCatalogItem> = Gson().fromJson(jsonString, type)
                            _diseasesState.value = DiseasesResult.Success(diseases)
                        } catch (e: Exception) {
                            Log.e("DiseaseViewModel", "Parsing error in fetchDiseases", e)
                            _diseasesState.value = DiseasesResult.Error("Data processing error: ${e.localizedMessage}")
                        }
                    } else {
                        val errorDetail = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: "Server error ${response.code()}"
                        Log.e("DiseaseViewModel", "Server error fetching diseases: $errorDetail")
                        _diseasesState.value = DiseasesResult.Error(errorDetail)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("DiseaseViewModel", "Network error fetching diseases", t)
                    _diseasesState.value = DiseasesResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun assignDisease(diseaseData: Map<String, Any>) {
        _addDiseaseState.value = AddDiseaseResult.Loading
        viewModelScope.launch {
            try {
                val body = Gson().toJson(diseaseData).toRequestBody("application/json".toMediaTypeOrNull())
                ApiClient.instance.assignDisease(body).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val jsonString = response.body()!!.string()
                                val body = Gson().fromJson(jsonString, AssignDiseaseResponse::class.java)
                                if (body?.error != null) {
                                    _addDiseaseState.value = AddDiseaseResult.Error(body.error)
                                } else {
                                    _addDiseaseState.value = AddDiseaseResult.Success
                                    val diseaseName = diseaseData["disease_name"]?.toString() ?: "Disease"
                                    addLocalNotification("New disease record assigned: $diseaseName")
                                }
                            } catch (e: Exception) {
                                Log.e("DiseaseViewModel", "Parsing error in assignDisease", e)
                                _addDiseaseState.value = AddDiseaseResult.Error("Response parsing error")
                            }
                        } else {
                            val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                            val errorMsg = if (errorBody.contains("<!DOCTYPE html>") || errorBody.contains("<html>")) {
                                "Server error: ${response.code()}. The endpoint might be incorrect or the server is down."
                            } else {
                                errorBody.takeIf { it.isNotBlank() } ?: "Failed to assign disease (Status: ${response.code()})"
                            }
                            Log.e("DiseaseViewModel", "Assign error: $errorMsg")
                            _addDiseaseState.value = AddDiseaseResult.Error(errorMsg)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("DiseaseViewModel", "Network Failure", t)
                        _addDiseaseState.value = AddDiseaseResult.Error("Network error: ${t.localizedMessage}")
                    }
                })
            } catch (e: Exception) {
                Log.e("DiseaseViewModel", "Launch error in assignDisease", e)
                _addDiseaseState.value = AddDiseaseResult.Error("Launch error: ${e.localizedMessage}")
            }
        }
    }

    fun addDisease(diseaseData: Map<String, Any>) {
        _addDiseaseState.value = AddDiseaseResult.Loading
        viewModelScope.launch {
            val bodyPayload = Gson().toJson(diseaseData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.addDisease(bodyPayload).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val body = Gson().fromJson(jsonString, GenericResponse::class.java)
                            if (body?.error != null) {
                                _addDiseaseState.value = AddDiseaseResult.Error(body.error)
                            } else {
                                _addDiseaseState.value = AddDiseaseResult.Success
                                val diseaseName = diseaseData["name"]?.toString() ?: "Disease"
                                addLocalNotification("New disease added to catalog: $diseaseName")
                                fetchDiseases()
                            }
                        } catch (e: Exception) {
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                        val errorMsg = if (errorBody.contains("<!DOCTYPE html>") || errorBody.contains("<html>")) {
                            "Server error. Please try again later."
                        } else {
                            errorBody.takeIf { it.isNotBlank() } ?: "Failed to add disease."
                        }
                        _addDiseaseState.value = AddDiseaseResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _addDiseaseState.value = AddDiseaseResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun updateDisease(diseaseId: Int, diseaseData: Map<String, Any>) {
        _updateDiseaseState.value = UpdateDiseaseResult.Loading
        viewModelScope.launch {
            val id = diseaseId
            val body = Gson().toJson(diseaseData).toRequestBody("application/json".toMediaTypeOrNull())
            ApiClient.instance.updateDisease(id, body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val body = Gson().fromJson(jsonString, GenericResponse::class.java)
                            if (body?.error != null) {
                                _updateDiseaseState.value = UpdateDiseaseResult.Error(body.error)
                            } else {
                                _updateDiseaseState.value = UpdateDiseaseResult.Success
                                val diseaseName = diseaseData["name"]?.toString() ?: "Disease"
                                addLocalNotification("Disease catalog entry updated: $diseaseName")
                                fetchDiseases()
                            }
                        } catch (e: Exception) {
                            Log.e("DiseaseViewModel", "Parsing error", e)
                            _updateDiseaseState.value = UpdateDiseaseResult.Error("Response parsing error")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                        val errorMsg = if (errorBody.contains("<!DOCTYPE html>") || errorBody.contains("<html>")) {
                            "Server error. Please try again later."
                        } else {
                            errorBody.takeIf { it.isNotBlank() } ?: "Failed to update disease."
                        }
                        _updateDiseaseState.value = UpdateDiseaseResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _updateDiseaseState.value = UpdateDiseaseResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun updatePatientDiseaseStatus(recordId: Int, updateData: Map<String, Any>) {
        _updateDiseaseState.value = UpdateDiseaseResult.Loading
        viewModelScope.launch {
            try {
                val body = Gson().toJson(updateData).toRequestBody("application/json".toMediaTypeOrNull())
                ApiClient.instance.updatePatientDiseaseStatus(recordId, body).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Log.i("API_DEBUG", "SERVER RESPONSE: ${response.code()}")
                        if (response.isSuccessful) {
                            _updateDiseaseState.value = UpdateDiseaseResult.Success
                            val status = updateData["status"] ?: "Updated"
                            addLocalNotification("Patient disease status updated to: $status")
                        } else {
                            val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                            Log.e("API_DEBUG", "ERROR BODY: $errorBody")
                            val lowercaseError = errorBody.lowercase()
                            val errorMsg = if (lowercaseError.contains("<!doctype html>") || lowercaseError.contains("<html>") || lowercaseError.contains("<html")) {
                                "Server error (Database or Path issue). Please check your backend connection."
                            } else {
                                errorBody.takeIf { it.isNotBlank() } ?: "Failed to update status."
                            }
                            _updateDiseaseState.value = UpdateDiseaseResult.Error(errorMsg)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("API_DEBUG", "NETWORK FAILURE", t)
                        _updateDiseaseState.value = UpdateDiseaseResult.Error("Network error: ${t.localizedMessage}")
                    }
                })
            } catch (e: Exception) {
                Log.e("DiseaseViewModel", "Launch error in updateStatus", e)
                _updateDiseaseState.value = UpdateDiseaseResult.Error("Launch error: ${e.localizedMessage}")
            }
        }
    }

    fun deletePatientDisease(recordId: Int, userId: Int? = null) {
        _deleteDiseaseState.value = DeleteDiseaseResult.Loading
        viewModelScope.launch {
            ApiClient.instance.deletePatientDisease(recordId, userId = userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _deleteDiseaseState.value = DeleteDiseaseResult.Success
                        addLocalNotification("Disease record (ID: $recordId) has been deleted")
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null } ?: ""
                        _deleteDiseaseState.value = DeleteDiseaseResult.Error("Failed to delete record: $errorBody")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _deleteDiseaseState.value = DeleteDiseaseResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun fetchDiseaseRecord(recordId: Int, userId: Int? = null) {
        _singleRecordState.value = SingleRecordResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getDiseaseRecord(recordId, userId = userId).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonString = response.body()!!.string()
                            val record = Gson().fromJson(jsonString, CaseRecord::class.java)
                            if (record != null) {
                                _singleRecordState.value = SingleRecordResult.Success(record)
                            } else {
                                _singleRecordState.value = SingleRecordResult.Error("Empty record response")
                            }
                        } catch (e: Exception) {
                            Log.e("DiseaseViewModel", "Parsing error in fetchDiseaseRecord", e)
                            _singleRecordState.value = SingleRecordResult.Error("Failed to parse record")
                        }
                    } else {
                        val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { null }
                        _singleRecordState.value = SingleRecordResult.Error(errorBody ?: "Record not found")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _singleRecordState.value = SingleRecordResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun resetAddDiseaseState() { _addDiseaseState.value = AddDiseaseResult.Idle }
    fun resetUpdateDiseaseState() { _updateDiseaseState.value = UpdateDiseaseResult.Idle }
    fun resetDeleteDiseaseState() { _deleteDiseaseState.value = DeleteDiseaseResult.Idle }
}

sealed class DiseasesResult {
    object Idle : DiseasesResult()
    object Loading : DiseasesResult()
    data class Success(val diseases: List<DiseaseCatalogItem>) : DiseasesResult()
    data class Error(val message: String) : DiseasesResult()
}

sealed class AddDiseaseResult {
    object Idle : AddDiseaseResult()
    object Loading : AddDiseaseResult()
    object Success : AddDiseaseResult()
    data class Error(val message: String) : AddDiseaseResult()
}

sealed class UpdateDiseaseResult {
    object Idle : UpdateDiseaseResult()
    object Loading : UpdateDiseaseResult()
    object Success : UpdateDiseaseResult()
    data class Error(val message: String) : UpdateDiseaseResult()
}

sealed class DeleteDiseaseResult {
    object Idle : DeleteDiseaseResult()
    object Loading : DeleteDiseaseResult()
    object Success : DeleteDiseaseResult()
    data class Error(val message: String) : DeleteDiseaseResult()
}

sealed class SingleRecordResult {
    object Idle : SingleRecordResult()
    object Loading : SingleRecordResult()
    data class Success(val record: CaseRecord) : SingleRecordResult()
    data class Error(val message: String) : SingleRecordResult()
}
