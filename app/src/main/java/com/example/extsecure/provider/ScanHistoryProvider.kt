package com.example.extsecure.provider

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.extsecure.database.ScanDatabase
import com.example.extsecure.database.ScanEntity
import kotlinx.coroutines.runBlocking

class ScanHistoryProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.extsecure.provider"
        const val PATH = "scans"

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH")

        val COLUMNS = arrayOf(
            "extensionId",
            "extensionName",
            "permissions",
            "riskScore",
            "riskLevel",
            "description",
            "version",
            "timestamp"
        )
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH, 1)
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {

        val scans = runBlocking {
            ScanDatabase.getInstance(context!!).scanDao().getAllScansOnce()
        }

        return MatrixCursor(COLUMNS).apply {

            scans.forEach { scan ->

                addRow(
                    arrayOf(
                        scan.extensionId,
                        scan.extensionName,
                        scan.permissions,
                        scan.riskScore,
                        scan.riskLevel,
                        scan.description,
                        scan.version,
                        scan.timestamp
                    )
                )
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        values ?: return null

        val entity = ScanEntity(
            extensionId = values.getAsString("extensionId"),
            extensionName = values.getAsString("extensionName"),
            permissions = values.getAsString("permissions") ?: "",
            riskScore = values.getAsFloat("riskScore"),
            riskLevel = values.getAsString("riskLevel"),
            description = values.getAsString("description"),
            version = values.getAsString("version"),
            timestamp = values.getAsLong("timestamp") ?: System.currentTimeMillis()
        )

        runBlocking {
            ScanDatabase.getInstance(context!!).scanDao().insertScan(entity)
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return CONTENT_URI
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {

        runBlocking {
            ScanDatabase.getInstance(context!!).scanDao().clearAll()
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)

        return 1
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/scans"
    }
}