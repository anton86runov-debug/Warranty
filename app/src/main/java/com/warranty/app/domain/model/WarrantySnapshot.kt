package com.warranty.app.domain.model

data class WarrantySnapshot(
    val item: WarrantyItem,
    val daysRemaining: Long?,
    val status: WarrantyStatus
)
