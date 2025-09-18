package com.warranty.app.domain.usecase

import com.warranty.app.domain.model.WarrantyItem
import com.warranty.app.domain.repository.WarrantyRepository
import javax.inject.Inject

class GetWarrantyUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    suspend operator fun invoke(id: Long): WarrantyItem? = repository.findById(id)
}
