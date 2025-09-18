package com.warranty.app.domain.usecase

import com.warranty.app.domain.model.WarrantySnapshot
import com.warranty.app.domain.repository.WarrantyRepository
import com.warranty.app.domain.util.WarrantyStatusCalculator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveWarrantiesUseCase @Inject constructor(
    private val repository: WarrantyRepository,
    private val calculator: WarrantyStatusCalculator
) {
    operator fun invoke(): Flow<List<WarrantySnapshot>> =
        repository.observe().map { items ->
            items.map { item ->
                val computed = calculator.compute(item)
                WarrantySnapshot(
                    item = item,
                    daysRemaining = computed.daysRemaining,
                    status = computed.status
                )
            }.sortedWith(compareBy { snapshot -> snapshot.daysRemaining ?: Long.MAX_VALUE })
        }
}
