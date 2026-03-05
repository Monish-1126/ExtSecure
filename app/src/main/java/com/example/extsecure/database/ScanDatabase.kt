package com.example.extsecure.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanEntity::class], version = 4, exportSchema = false)
abstract class ScanDatabase : RoomDatabase() {

    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile private var INSTANCE: ScanDatabase? = null

        fun getInstance(context: Context): ScanDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ScanDatabase::class.java,
                    "scan_database"
                ).build().also { INSTANCE = it }
            }
    }
}