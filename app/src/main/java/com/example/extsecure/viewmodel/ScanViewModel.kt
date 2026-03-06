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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
// ── UI state ──────────────────────────────────────────────────────────────────
data class BatchScanResult(
    val extensionId: String,
    val response: AnalyzeResponse? = null,
    val error: String? = null
)
sealed interface ScanUiState {
    data object Idle    : ScanUiState
    data object Loading : ScanUiState
    data class Success(val responses: List<BatchScanResult>) : ScanUiState

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
    fun analyzeExtensionsBatch(extensionIds: List<String>) {

        if (extensionIds.isEmpty()) {
            _uiState.value = ScanUiState.Error("No extension IDs provided")
            return
        }

        _uiState.value = ScanUiState.Loading

        viewModelScope.launch {

            try {

                val results = extensionIds.map { id ->

                    async(Dispatchers.IO) {

                        try {

                            val response = RetrofitClient.apiService
                                .analyzeExtension(AnalyzeRequest(id))

                            if (response.isSuccessful) {

                                val body = response.body()

                                if (body != null) {

                                    val values = ContentValues().apply {
                                        put("extensionId", body.extensionId)
                                        put("extensionName", body.extensionName)
                                        put("permissions", body.permissions?.joinToString(",") ?: "")
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

                                    BatchScanResult(
                                        extensionId = id,
                                        response = body
                                    )

                                } else {
                                    BatchScanResult(
                                        extensionId = id,
                                        error = "Empty response"
                                    )
                                }

                            } else {

                                val errorBody = response.errorBody()?.string()

                                val message = try {
                                    val json = JSONObject(errorBody ?: "")
                                    json.optString("detail", "Extension not found")
                                } catch (e: Exception) {
                                    "Extension not found"
                                }

                                BatchScanResult(
                                    extensionId = id,
                                    error = message
                                )
                            }

                        } catch (e: Exception) {

                            BatchScanResult(
                                extensionId = id,
                                error = e.localizedMessage ?: "Network error"
                            )
                        }
                    }

                }.awaitAll()

                _uiState.value = ScanUiState.Success(results)

            } catch (e: Exception) {

                _uiState.value = ScanUiState.Error(
                    e.localizedMessage ?: "Batch scan failed"
                )
            }
        }
    }
}