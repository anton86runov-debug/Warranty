package com.warranty.app.ui.edit

import java.time.LocalDate

data class EditWarrantyState(
    val id: Long? = null,
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val store: String = "",
    val purchaseDate: LocalDate = LocalDate.now(),
    val expirationDate: LocalDate? = null,
    val durationMonths: String = "",
    val reminderEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
