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

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val response = RetrofitClient.apiService
                    .analyzeExtension(AnalyzeRequest(extensionId))

                val values = ContentValues().apply {
                    put("extensionId", response.extensionId)
                    put("extensionName", response.extensionName)
                    put("permissions", response.permissions?.joinToString(",") ?: "")
                    put("riskScore", response.riskScore)
                    put("riskLevel", response.riskLevel)
                    put("description", response.description)
                    put("version", response.version)
                    put("timestamp", System.currentTimeMillis())
                }

                getApplication<Application>().contentResolver.insert(
                    ScanHistoryProvider.CONTENT_URI,
                    values
                )

                _uiState.value = ScanUiState.Success(response)

            } catch (e: Exception) {

                e.printStackTrace()

                _uiState.value = ScanUiState.Error(
                    e.message ?: "Failed to analyze extension"
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