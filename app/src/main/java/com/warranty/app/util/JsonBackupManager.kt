package com.warranty.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.warranty.app.domain.model.WarrantyItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class JsonBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val clock: Clock
) {
    suspend fun export(items: List<WarrantyItem>, fileName: String? = null): Uri =
        withContext(Dispatchers.IO) {
            val safeFileName = fileName ?: buildFileName()
            val payload = json.encodeToString(items)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportWithMediaStore(safeFileName, payload)
            } else {
                exportLegacy(safeFileName, payload)
            }
        }

    suspend fun import(uri: Uri): List<WarrantyItem> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val content = resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IOException("Unable to read from URI: ")
        json.decodeFromString(content)
    }

    private fun buildFileName(): String {
        val timestamp = LocalDateTime.now(clock).toString().replace(":", "-")
        return "warranties-.json"
    }

    private fun exportWithMediaStore(fileName: String, payload: String): Uri {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
        }
        val uri = resolver.insert(collection, contentValues)
            ?: throw IOException("Unable to create file in Downloads")
        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(payload.toByteArray())
            stream.flush()
        } ?: throw IOException("Unable to open stream for URI: ")
        return uri
    }

    private fun exportLegacy(fileName: String, payload: String): Uri {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val target = File(downloadsDir, fileName)
        target.writeText(payload)
        return Uri.fromFile(target)
    }
}
