package com.example.extsecure.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScanDao {

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): LiveData<List<ScanEntity>>

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    suspend fun getAllScansOnce(): List<ScanEntity>

    @Query("SELECT * FROM scan_history WHERE extensionId = :extensionId LIMIT 1")
    suspend fun getScanByExtensionId(extensionId: String): ScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}