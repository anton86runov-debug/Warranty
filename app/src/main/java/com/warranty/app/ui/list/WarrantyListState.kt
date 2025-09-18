package com.warranty.app.ui.list

import com.warranty.app.domain.model.WarrantyFilter
import com.warranty.app.domain.model.WarrantyStatus
import java.time.LocalDate

data class WarrantyListState(
    val items: List<WarrantyUiModel> = emptyList(),
    val filter: WarrantyFilter = WarrantyFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val message: String? = null
)

data class WarrantyUiModel(
    val id: Long,
    val name: String,
    val category: String?,
    val store: String?,
    val price: Double?,
    val daysRemaining: Long?,
    val status: WarrantyStatus,
    val reminderEnabled: Boolean,
    val expirationDate: LocalDate?
)
