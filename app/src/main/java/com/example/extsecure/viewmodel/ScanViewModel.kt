package com.example.extsecure.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.extsecure.api.AnalyzeRequest
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.api.RetrofitClient
import com.example.extsecure.database.ScanDatabase
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.repository.ScanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

// Result for one extension scan
data class BatchScanResult(
    val extensionId: String,
    val response: AnalyzeResponse? = null,
    val error: String? = null
)

// UI state for scan screen
sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Loading : ScanUiState
    data class Success(val responses: List<BatchScanResult>) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScanRepository(application.applicationContext)
    private val dao = ScanDatabase.getInstance(application).scanDao()

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    // Scan history as reactive Flow backed by ContentProvider notifications
    val scanHistoryFlow: StateFlow<List<ScanEntity>> =
        repository.observeScansViaProvider()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateNetworkStatus(isConnected: Boolean) {
        _isNetworkAvailable.value = isConnected
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistoryViaProvider()
        }
    }

    fun getScanByExtensionId(extensionId: String): LiveData<ScanEntity?> {
        return liveData(Dispatchers.IO) {
            emit(repository.getScanByExtensionId(extensionId))
        }
    }

    // Analyze multiple extensions in parallel via API, save via ContentProvider
    fun analyzeExtensionsBatch(extensionIds: List<String>) {

        if (extensionIds.isEmpty()) {
            _uiState.value = ScanUiState.Error("No extension IDs provided")
            return
        }

        if (!_isNetworkAvailable.value) {
            _uiState.value = ScanUiState.Error("No internet connection. Please check your network.")
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
                                    // ── Save via Content Provider ──
                                    repository.saveScanViaProvider(body)

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