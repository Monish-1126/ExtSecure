package com.example.extsecure.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val extensionId: String,
    val extensionName: String,
    val riskScore: Float,
    val riskLevel: String,
    val description: String,
    val version: String,
    val timestamp: Long = System.currentTimeMillis()
)