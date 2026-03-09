package com.example.extsecure.repository

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.extsecure.api.AnalyzeRequest
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.api.RetrofitClient
import com.example.extsecure.database.ScanDatabase
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.provider.ScanHistoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Repository that ties together:
 *  - Web API calls via Retrofit (suspend / coroutines)
 *  - Content Provider for insert / query / delete via ContentResolver
 *  - Room DAO for Flow-based reactive queries
 */
class ScanRepository(private val context: Context) {

    private val dao = ScanDatabase.getInstance(context).scanDao()

    // ───────────────────────────────────────────────────────
    //  WEB API  —  Retrofit + Coroutines
    // ───────────────────────────────────────────────────────

    /** Call the remote API inside an IO coroutine. */
    suspend fun analyzeExtension(extensionId: String): AnalyzeResponse {
        return withContext(Dispatchers.IO) {
            val response = RetrofitClient.apiService.analyzeExtension(AnalyzeRequest(extensionId))
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw Exception(response.errorBody()?.string() ?: "Analysis failed")
            }
        }
    }

    // ───────────────────────────────────────────────────────
    //  CONTENT PROVIDER  —  Insert via ContentResolver
    // ───────────────────────────────────────────────────────

    /** Save a scan result through the Content Provider. */
    suspend fun saveScanViaProvider(body: AnalyzeResponse) {
        withContext(Dispatchers.IO) {
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
            context.contentResolver.insert(ScanHistoryProvider.CONTENT_URI, values)
        }
    }

    // ───────────────────────────────────────────────────────
    //  CONTENT PROVIDER  —  Query via ContentResolver
    // ───────────────────────────────────────────────────────

    /** Read all scans through the Content Provider (one-shot). */
    suspend fun getScansViaProvider(): List<ScanEntity> {
        return withContext(Dispatchers.IO) {
            val scans = mutableListOf<ScanEntity>()
            val cursor = context.contentResolver.query(
                ScanHistoryProvider.CONTENT_URI,
                null, null, null, null
            )
            cursor?.use {
                while (it.moveToNext()) {
                    scans.add(
                        ScanEntity(
                            extensionId = it.getString(it.getColumnIndexOrThrow("extensionId")),
                            extensionName = it.getString(it.getColumnIndexOrThrow("extensionName")),
                            permissions = it.getString(it.getColumnIndexOrThrow("permissions")),
                            riskScore = it.getFloat(it.getColumnIndexOrThrow("riskScore")),
                            riskLevel = it.getString(it.getColumnIndexOrThrow("riskLevel")),
                            description = it.getString(it.getColumnIndexOrThrow("description")),
                            version = it.getString(it.getColumnIndexOrThrow("version")),
                            timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                        )
                    )
                }
            }
            scans
        }
    }

    // ───────────────────────────────────────────────────────
    //  CONTENT PROVIDER  —  Delete via ContentResolver
    // ───────────────────────────────────────────────────────

    /** Clear all scan history through the Content Provider. */
    suspend fun clearHistoryViaProvider() {
        withContext(Dispatchers.IO) {
            context.contentResolver.delete(ScanHistoryProvider.CONTENT_URI, null, null)
        }
    }

    // ───────────────────────────────────────────────────────
    //  CONTENT PROVIDER  —  Observe changes as a Flow
    // ───────────────────────────────────────────────────────

    /**
     * Returns a Flow that emits the latest scan list every time
     * the Content Provider's URI is notified of a change.
     * Combines ContentObserver with Kotlin coroutine callbackFlow.
     */
    fun observeScansViaProvider(): Flow<List<ScanEntity>> = callbackFlow {
        // Emit initial data
        trySend(getScansViaProviderSync())

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(getScansViaProviderSync())
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(getScansViaProviderSync())
            }
        }

        context.contentResolver.registerContentObserver(
            ScanHistoryProvider.CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    /** Blocking read for use inside ContentObserver callback. */
    private fun getScansViaProviderSync(): List<ScanEntity> {
        val scans = mutableListOf<ScanEntity>()
        val cursor = context.contentResolver.query(
            ScanHistoryProvider.CONTENT_URI,
            null, null, null, null
        )
        cursor?.use {
            while (it.moveToNext()) {
                scans.add(
                    ScanEntity(
                        extensionId = it.getString(it.getColumnIndexOrThrow("extensionId")),
                        extensionName = it.getString(it.getColumnIndexOrThrow("extensionName")),
                        permissions = it.getString(it.getColumnIndexOrThrow("permissions")),
                        riskScore = it.getFloat(it.getColumnIndexOrThrow("riskScore")),
                        riskLevel = it.getString(it.getColumnIndexOrThrow("riskLevel")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        version = it.getString(it.getColumnIndexOrThrow("version")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                    )
                )
            }
        }
        return scans
    }

    // ───────────────────────────────────────────────────────
    //  ROOM DAO  —  Direct access (for detail screen etc.)
    // ───────────────────────────────────────────────────────

    suspend fun getScanByExtensionId(extensionId: String): ScanEntity? {
        return withContext(Dispatchers.IO) {
            dao.getScanByExtensionId(extensionId)
        }
    }
}

