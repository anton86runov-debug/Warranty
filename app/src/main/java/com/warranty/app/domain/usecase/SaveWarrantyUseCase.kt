package com.warranty.app.domain.usecase

import com.warranty.app.domain.model.WarrantyItem
import com.warranty.app.domain.repository.WarrantyRepository
import javax.inject.Inject

class SaveWarrantyUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    suspend operator fun invoke(item: WarrantyItem): Long {
        require(item.expirationDate != null || item.durationMonths != null) {
            "Either expirationDate or durationMonths must be provided"
        }
        return repository.upsert(item)
    }
}
