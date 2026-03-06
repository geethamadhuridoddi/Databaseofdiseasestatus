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

class DiseaseViewModel : ViewModel() {

    private val _diseasesState = MutableStateFlow<DiseasesResult>(DiseasesResult.Idle)
    val diseasesState: StateFlow<DiseasesResult> = _diseasesState

    private val _addDiseaseState = MutableStateFlow<AddDiseaseResult>(AddDiseaseResult.Idle)
    val addDiseaseState: StateFlow<AddDiseaseResult> = _addDiseaseState

    private val _updateDiseaseState = MutableStateFlow<UpdateDiseaseResult>(UpdateDiseaseResult.Idle)
    val updateDiseaseState: StateFlow<UpdateDiseaseResult> = _updateDiseaseState

    fun fetchDiseases() {
        _diseasesState.value = DiseasesResult.Loading
        viewModelScope.launch {
            ApiClient.instance.getDiseases().enqueue(object : Callback<List<DiseaseCatalogItem>> {
                override fun onResponse(call: Call<List<DiseaseCatalogItem>>, response: Response<List<DiseaseCatalogItem>>) {
                    if (response.isSuccessful && response.body() != null) {
                        _diseasesState.value = DiseasesResult.Success(response.body()!!)
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to fetch diseases."
                        _diseasesState.value = DiseasesResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<List<DiseaseCatalogItem>>, t: Throwable) {
                    _diseasesState.value = DiseasesResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun addDisease(diseaseData: Map<String, Any>) {
        _addDiseaseState.value = AddDiseaseResult.Loading
        viewModelScope.launch {
            ApiClient.instance.addDisease(diseaseData).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        _addDiseaseState.value = AddDiseaseResult.Success
                        fetchDiseases()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to add disease."
                        _addDiseaseState.value = AddDiseaseResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    _addDiseaseState.value = AddDiseaseResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun updateDisease(diseaseId: Int, diseaseData: Map<String, Any>) {
        _updateDiseaseState.value = UpdateDiseaseResult.Loading
        viewModelScope.launch {
            ApiClient.instance.updateDisease(diseaseId, diseaseData).enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful) {
                        _updateDiseaseState.value = UpdateDiseaseResult.Success
                        fetchDiseases()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to update disease."
                        _updateDiseaseState.value = UpdateDiseaseResult.Error(errorMsg)
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    _updateDiseaseState.value = UpdateDiseaseResult.Error("Network error: ${t.localizedMessage}")
                }
            })
        }
    }

    fun resetAddDiseaseState() { _addDiseaseState.value = AddDiseaseResult.Idle }
    fun resetUpdateDiseaseState() { _updateDiseaseState.value = UpdateDiseaseResult.Idle }
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
