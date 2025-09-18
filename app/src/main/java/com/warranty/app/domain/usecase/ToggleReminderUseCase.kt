package com.warranty.app.domain.usecase

import com.warranty.app.domain.repository.WarrantyRepository
import javax.inject.Inject

class ToggleReminderUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        val existing = repository.findById(id) ?: return
        repository.upsert(existing.copy(reminderEnabled = enabled))
    }
}
