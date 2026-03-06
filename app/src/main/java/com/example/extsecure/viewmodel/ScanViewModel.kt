package com.example.extsecure.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import androidx.lifecycle.*
import com.example.extsecure.api.AnalyzeRequest
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.api.RetrofitClient
import com.example.extsecure.database.ScanDatabase
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.provider.ScanHistoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import org.json.JSONObject

// ── UI state ──────────────────────────────────────────────────────────────────
sealed interface ScanUiState {
    data object Idle    : ScanUiState
    data object Loading : ScanUiState
    data class  Success(val response: AnalyzeResponse) : ScanUiState
    data class  Error(val message: String)             : ScanUiState
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ScanDatabase.getInstance(application).scanDao()

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    val scanHistory: LiveData<List<ScanEntity>> = dao.getAllScans()

    fun updateNetworkStatus(isConnected: Boolean) {
        _isNetworkAvailable.value = isConnected
    }

    fun analyzeExtension(extensionId: String) {

        if (extensionId.isBlank()) {
            _uiState.value = ScanUiState.Error("Extension ID cannot be empty")
            return
        }

        _uiState.value = ScanUiState.Loading

        viewModelScope.launch {

            try {

                val response = RetrofitClient.apiService
                    .analyzeExtension(AnalyzeRequest(extensionId))

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null) {

                        val values = ContentValues().apply {
                            put("extensionId", body.extensionId)
                            put("extensionName", body.extensionName)
                            put("permissions", body.permissions?.joinToString(",")?:"")
                            put("riskScore", body.riskScore)
                            put("riskLevel", body.riskLevel)
                            put("description", body.description)
                            put("version", body.version)
                            put("timestamp", System.currentTimeMillis())
                        }

                        getApplication<Application>().contentResolver.insert(
                            ScanHistoryProvider.CONTENT_URI,
                            values
                        )

                        _uiState.value = ScanUiState.Success(body)

                    } else {
                        _uiState.value = ScanUiState.Error("Empty response from server")
                    }

                } else {

                    val errorBody = response.errorBody()?.string()

                    val message = try {

                        if (!errorBody.isNullOrEmpty()) {
                            val json = JSONObject(errorBody)

                            when {
                                json.has("error") -> json.getString("error")
                                json.has("detail") -> json.getString("detail")
                                else -> "Extension not found"
                            }

                        } else {
                            "Extension not found in Chrome Web Store"
                        }

                    } catch (e: Exception) {
                        "Extension not found in Chrome Web Store"
                    }

                    _uiState.value = ScanUiState.Error(message)
                }

            } catch (e: Exception) {

                Log.e("EXTSECURE_API", "API ERROR", e)

                _uiState.value = ScanUiState.Error(
                    e.localizedMessage ?: "Network error"
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) { dao.clearAll() }
    }

    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }
    fun getScanByExtensionId(extensionId: String): LiveData<ScanEntity?> {
        return liveData {
            emit(dao.getScanByExtensionId(extensionId))
        }
    }
}