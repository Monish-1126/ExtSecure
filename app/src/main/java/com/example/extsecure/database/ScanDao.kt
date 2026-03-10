package com.example.extsecure.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScanDao {

    // LiveData query for all scans ordered by timestamp.
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): LiveData<List<ScanEntity>>

    // One-shot query for ContentProvider.
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    suspend fun getAllScansOnce(): List<ScanEntity>

    // Fetch single scan by ID.
    @Query("SELECT * FROM scan_history WHERE extensionId = :extensionId LIMIT 1")
    suspend fun getScanByExtensionId(extensionId: String): ScanEntity?

    // Insert or replace scan.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    // Delete all scans.
    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}