package com.warranty.app.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warranty.app.domain.model.WarrantyItem
import com.warranty.app.domain.usecase.GetWarrantyUseCase
import com.warranty.app.domain.usecase.SaveWarrantyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditWarrantyViewModel @Inject constructor(
    private val getWarrantyUseCase: GetWarrantyUseCase,
    private val saveWarrantyUseCase: SaveWarrantyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditWarrantyState())
    val state: StateFlow<EditWarrantyState> = _state.asStateFlow()

    fun load(id: Long?) {
        if (id == null || _state.value.id == id) return
        viewModelScope.launch {
            val item = getWarrantyUseCase(id)
            if (item != null) {
                _state.update {
                    it.copy(
                        id = item.id,
                        name = item.name,
                        category = item.category.orEmpty(),
                        price = item.price?.toString().orEmpty(),
                        store = item.store.orEmpty(),
                        purchaseDate = item.purchaseDate,
                        expirationDate = item.expirationDate,
                        durationMonths = item.durationMonths?.toString().orEmpty(),
                        reminderEnabled = item.reminderEnabled,
                        isSaved = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = updateField { copy(name = value) }
    fun onCategoryChange(value: String) = updateField { copy(category = value) }
    fun onPriceChange(value: String) = updateField { copy(price = value) }
    fun onStoreChange(value: String) = updateField { copy(store = value) }
    fun onPurchaseDateChange(value: LocalDate) = updateField { copy(purchaseDate = value) }
    fun onExpirationDateChange(value: LocalDate?) = updateField { copy(expirationDate = value) }
    fun onDurationMonthsChange(value: String) = updateField { copy(durationMonths = value) }
    fun onReminderChange(value: Boolean) = updateField { copy(reminderEnabled = value) }

    fun onSave() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Name is required") }
            return
        }

        val price = current.price.normalizeNumber()?.toDoubleOrNull()
        val duration = current.durationMonths.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
        val expiration = current.expirationDate

        if (expiration == null && (duration == null || duration <= 0)) {
            _state.update { it.copy(errorMessage = "Provide expiration date or duration") }
            return
        }

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        val payload = WarrantyItem(
            id = current.id ?: 0,
            name = current.name.trim(),
            category = current.category.trim().ifEmpty { null },
            price = price,
            store = current.store.trim().ifEmpty { null },
            purchaseDate = current.purchaseDate,
            expirationDate = expiration,
            durationMonths = duration,
            reminderEnabled = current.reminderEnabled
        )

        viewModelScope.launch {
            runCatching { saveWarrantyUseCase(payload) }
                .onSuccess { newId ->
                    _state.update {
                        it.copy(
                            id = it.id ?: newId,
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.localizedMessage ?: "Unable to save"
                        )
                    }
                }
        }
    }

    fun consumeError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun consumeSaved() {
        _state.update { it.copy(isSaved = false) }
    }

    private fun updateField(block: EditWarrantyState.() -> EditWarrantyState) {
        _state.update { it.block() }
    }

    private fun String.normalizeNumber(): String = replace(',', '.')
}
