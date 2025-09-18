package com.warranty.app.ui.list

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warranty.app.domain.model.WarrantyFilter
import com.warranty.app.domain.model.WarrantySnapshot
import com.warranty.app.domain.model.WarrantyStatus
import com.warranty.app.domain.usecase.DeleteWarrantyUseCase
import com.warranty.app.domain.usecase.ExportWarrantiesUseCase
import com.warranty.app.domain.usecase.ImportWarrantiesUseCase
import com.warranty.app.domain.usecase.ObserveWarrantiesUseCase
import com.warranty.app.domain.usecase.ToggleReminderUseCase
import com.warranty.app.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WarrantyListViewModel @Inject constructor(
    observeWarrantiesUseCase: ObserveWarrantiesUseCase,
    private val deleteWarrantyUseCase: DeleteWarrantyUseCase,
    private val toggleReminderUseCase: ToggleReminderUseCase,
    private val exportWarrantiesUseCase: ExportWarrantiesUseCase,
    private val importWarrantiesUseCase: ImportWarrantiesUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val snapshots = MutableStateFlow<List<WarrantySnapshot>>(emptyList())
    private val filter = MutableStateFlow(WarrantyFilter.ALL)
    private val query = MutableStateFlow("")
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<WarrantyListState> = combine(
        snapshots,
        filter,
        query,
        message
    ) { currentSnapshots, currentFilter, currentQuery, currentMessage ->
        val filtered = currentSnapshots
            .filter { matchesFilter(it, currentFilter) }
            .filter { matchesQuery(it, currentQuery) }
            .map { snapshot ->
                WarrantyUiModel(
                    id = snapshot.item.id,
                    name = snapshot.item.name,
                    category = snapshot.item.category,
                    store = snapshot.item.store,
                    price = snapshot.item.price,
                    daysRemaining = snapshot.daysRemaining,
                    status = snapshot.status,
                    reminderEnabled = snapshot.item.reminderEnabled,
                    expirationDate = snapshot.item.resolvedExpirationDate()
                )
            }

        WarrantyListState(
            items = filtered,
            filter = currentFilter,
            searchQuery = currentQuery,
            isLoading = false,
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WarrantyListState()
    )

    init {
        viewModelScope.launch {
            observeWarrantiesUseCase().collect { emitted ->
                snapshots.value = emitted
            }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: WarrantyFilter) {
        filter.value = value
    }

    fun onReminderToggle(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            toggleReminderUseCase(id, enabled)
            reminderScheduler.triggerImmediateCheck()
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch {
            deleteWarrantyUseCase(id)
            message.update { "Warranty removed" }
        }
    }

    fun onExport() {
        viewModelScope.launch {
            runCatching { exportWarrantiesUseCase() }
                .onSuccess { uri -> message.value = "Exported to " }
                .onFailure { error -> message.value = error.localizedMessage ?: "Export failed" }
        }
    }

    fun onImport(uri: Uri, replaceExisting: Boolean) {
        viewModelScope.launch {
            runCatching { importWarrantiesUseCase(uri, replaceExisting) }
                .onSuccess { count ->
                    message.value = "Imported  warranties"
                    reminderScheduler.triggerImmediateCheck()
                }
                .onFailure { error -> message.value = error.localizedMessage ?: "Import failed" }
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    private fun matchesFilter(snapshot: WarrantySnapshot, filter: WarrantyFilter): Boolean = when (filter) {
        WarrantyFilter.ALL -> true
        WarrantyFilter.EXPIRING_SOON -> snapshot.status == WarrantyStatus.EXPIRING_SOON
        WarrantyFilter.ACTIVE -> snapshot.status == WarrantyStatus.ACTIVE
        WarrantyFilter.EXPIRED -> snapshot.status == WarrantyStatus.EXPIRED
    }

    private fun matchesQuery(snapshot: WarrantySnapshot, query: String): Boolean {
        if (query.isBlank()) return true
        val normalized = query.trim().lowercase()
        return listOfNotNull(
            snapshot.item.name,
            snapshot.item.category,
            snapshot.item.store
        ).any { it.lowercase().contains(normalized) }
    }
}
