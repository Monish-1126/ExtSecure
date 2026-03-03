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
        const val PATH      = "scans"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH")
        val COLUMNS = arrayOf("id", "extensionId", "riskScore", "riskLevel", "timestamp")
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH,     1)   // all rows
        addURI(AUTHORITY, "$PATH/#", 2)  // single row
    }

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = when (uriMatcher.match(uri)) {
        1    -> "vnd.android.cursor.dir/scans"
        2    -> "vnd.android.cursor.item/scans"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

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
                addRow(arrayOf(scan.id, scan.extension_id, scan.riskScore, scan.riskLevel, scan.timestamp))
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values ?: return null
        val entity = ScanEntity(
            extension_id = values.getAsString("extensionId").orEmpty(),
            riskScore   = values.getAsFloat("riskScore")   ?: 0f,
            riskLevel   = values.getAsString("riskLevel").orEmpty(),
            timestamp   = values.getAsLong("timestamp")    ?: System.currentTimeMillis()
        )
        runBlocking { ScanDatabase.getInstance(context!!).scanDao().insertScan(entity) }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return ContentUris.withAppendedId(CONTENT_URI, entity.id.toLong())
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (uriMatcher.match(uri) != 2) return 0
        val id = ContentUris.parseId(uri).toInt()
        runBlocking { ScanDatabase.getInstance(context!!).scanDao().deleteScan(id) }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return 1
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}