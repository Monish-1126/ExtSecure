package com.example.extsecure.repository

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.extsecure.api.AnalyzeResponse
import com.example.extsecure.database.ScanDatabase
import com.example.extsecure.database.ScanEntity
import com.example.extsecure.provider.ScanHistoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Repository layer coordinating Web API, ContentProvider, and Room database.
 */
class ScanRepository(private val context: Context) {

    private val dao = ScanDatabase.getInstance(context).scanDao()

    // Save scan result through ContentProvider to database.
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

    // Query all scans through ContentProvider (one-shot).
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

    // Clear all scan history through ContentProvider.
    suspend fun clearHistoryViaProvider() {
        withContext(Dispatchers.IO) {
            context.contentResolver.delete(ScanHistoryProvider.CONTENT_URI, null, null)
        }
    }

    // Observe scan changes via ContentProvider notifications as a Flow.
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

    // Fetch one scan by ID directly from Room.
    suspend fun getScanByExtensionId(extensionId: String): ScanEntity? {
        return withContext(Dispatchers.IO) {
            dao.getScanByExtensionId(extensionId)
        }
    }
}

