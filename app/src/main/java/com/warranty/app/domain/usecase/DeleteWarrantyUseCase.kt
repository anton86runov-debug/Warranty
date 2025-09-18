package com.warranty.app.domain.usecase

import com.warranty.app.domain.repository.WarrantyRepository
import javax.inject.Inject

class DeleteWarrantyUseCase @Inject constructor(
    private val repository: WarrantyRepository
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
