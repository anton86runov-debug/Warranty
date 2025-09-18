package com.warranty.app.domain.model

import com.warranty.app.domain.util.LocalDateIsoSerializer
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class WarrantyItem(
    val id: Long = 0,
    val name: String,
    val category: String? = null,
    val price: Double? = null,
    val store: String? = null,
    @Serializable(with = LocalDateIsoSerializer::class)
    val purchaseDate: LocalDate,
    @Serializable(with = LocalDateIsoSerializer::class)
    val expirationDate: LocalDate? = null,
    val durationMonths: Int? = null,
    val reminderEnabled: Boolean = true
) {
    fun resolvedExpirationDate(): LocalDate? =
        expirationDate ?: durationMonths?.let { purchaseDate.plusMonths(it.toLong()) }
}
