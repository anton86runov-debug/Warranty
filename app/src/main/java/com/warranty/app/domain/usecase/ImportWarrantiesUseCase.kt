package com.warranty.app.domain.usecase

import android.net.Uri
import com.warranty.app.domain.repository.WarrantyRepository
import com.warranty.app.util.JsonBackupManager
import javax.inject.Inject

class ImportWarrantiesUseCase @Inject constructor(
    private val repository: WarrantyRepository,
    private val backupManager: JsonBackupManager
) {
    suspend operator fun invoke(uri: Uri, replaceExisting: Boolean = false): Int {
        val imported = backupManager.import(uri).map { it.copy(id = 0) }
        if (replaceExisting) {
            repository.clear()
        }
        repository.upsert(imported)
        return imported.size
    }
}
