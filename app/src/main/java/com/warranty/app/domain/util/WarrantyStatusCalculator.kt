package com.warranty.app.domain.util

import com.warranty.app.domain.model.WarrantyItem
import com.warranty.app.domain.model.WarrantyStatus
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class WarrantyComputedState(
    val daysRemaining: Long?,
    val status: WarrantyStatus
)

@Singleton
class WarrantyStatusCalculator @Inject constructor(
    private val clock: Clock
) {
    fun compute(item: WarrantyItem): WarrantyComputedState {
        val today = LocalDate.now(clock)
        val expiration = item.resolvedExpirationDate()
        val daysRemaining = expiration?.let { ChronoUnit.DAYS.between(today, it) }

        val status = when {
            expiration == null -> WarrantyStatus.ACTIVE
            daysRemaining != null && daysRemaining < 0 -> WarrantyStatus.EXPIRED
            daysRemaining != null && daysRemaining <= 30 -> WarrantyStatus.EXPIRING_SOON
            else -> WarrantyStatus.ACTIVE
        }

        return WarrantyComputedState(daysRemaining = daysRemaining, status = status)
    }
}
