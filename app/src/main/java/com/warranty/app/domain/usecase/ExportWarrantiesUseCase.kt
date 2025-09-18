package com.warranty.app.domain.usecase

import android.net.Uri
import com.warranty.app.domain.repository.WarrantyRepository
import com.warranty.app.util.JsonBackupManager
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class ExportWarrantiesUseCase @Inject constructor(
    private val repository: WarrantyRepository,
    private val backupManager: JsonBackupManager
) {
    suspend operator fun invoke(fileName: String? = null): Uri {
        val items = repository.observe().first()
        return backupManager.export(items, fileName)
    }
}
