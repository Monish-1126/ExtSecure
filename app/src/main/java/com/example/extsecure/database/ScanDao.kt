package com.example.extsecure.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    /** LiveData-based reactive query (used by existing screens). */
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): LiveData<List<ScanEntity>>

    /** Flow-based reactive query (coroutines). */
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScansFlow(): Flow<List<ScanEntity>>

    /** One-shot coroutine query (used by Content Provider). */
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    suspend fun getAllScansOnce(): List<ScanEntity>

    @Query("SELECT * FROM scan_history WHERE extensionId = :extensionId LIMIT 1")
    suspend fun getScanByExtensionId(extensionId: String): ScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}