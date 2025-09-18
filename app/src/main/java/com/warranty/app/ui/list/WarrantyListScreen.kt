package com.warranty.app.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.warranty.app.R
import com.warranty.app.domain.model.WarrantyFilter
import com.warranty.app.ui.components.WarrantyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarrantyListScreen(
    state: WarrantyListState,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onReminderToggle: (Long, Boolean) -> Unit,
    onFilterChange: (WarrantyFilter) -> Unit,
    onQueryChange: (String) -> Unit,
    onExport: () -> Unit,
    onImportSelected: (Boolean) -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var queryState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(state.searchQuery))
    }

    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery != queryState.text) {
            queryState = TextFieldValue(state.searchQuery)
        }
    }

    LaunchedEffect(state.message) {
        val message = state.message
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    onImportSelected(true)
                }) {
                    Text(text = stringResource(id = R.string.import_replace_yes))
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = {
                        showImportDialog = false
                        onImportSelected(false)
                    }) {
                        Text(text = stringResource(id = R.string.import_replace_no))
                    }
                    TextButton(onClick = { showImportDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            },
            title = { Text(text = stringResource(id = R.string.import_label)) },
            text = { Text(text = stringResource(id = R.string.import_replace)) }
        )
    }

    val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onExport) {
                        Icon(imageVector = Icons.Outlined.Upload, contentDescription = stringResource(id = R.string.export_label))
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(imageVector = Icons.Outlined.Download, contentDescription = stringResource(id = R.string.import_label))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(onClick = onAddClick) {
                Text(text = stringResource(id = R.string.add_warranty))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = queryState,
                onValueChange = { value ->
                    queryState = value
                    onQueryChange(value.text)
                },
                placeholder = { Text(text = stringResource(id = R.string.search_hint)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterRow(
                selected = state.filter,
                onFilterChange = onFilterChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            WarrantyListContent(
                state = state,
                onItemClick = onItemClick,
                onDelete = onDelete,
                onReminderToggle = onReminderToggle
            )
        }
    }
}

@Composable
private fun FilterRow(
    selected: WarrantyFilter,
    onFilterChange: (WarrantyFilter) -> Unit
) {
    val filters = remember { WarrantyFilter.values() }
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters.size) { index ->
            val filter = filters[index]
            FilterChip(
                selected = selected == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(text = filterLabel(filter)) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

@Composable
private fun filterLabel(filter: WarrantyFilter): String = when (filter) {
    WarrantyFilter.ALL -> stringResource(id = R.string.filter_all)
    WarrantyFilter.EXPIRING_SOON -> stringResource(id = R.string.filter_expiring)
    WarrantyFilter.ACTIVE -> stringResource(id = R.string.filter_active)
    WarrantyFilter.EXPIRED -> stringResource(id = R.string.filter_expired)
}

@Composable
private fun WarrantyListContent(
    state: WarrantyListState,
    onItemClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onReminderToggle: (Long, Boolean) -> Unit
) {
    val lazyListState = rememberLazyListState()
    if (state.items.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.list_empty_state))
        }
    } else {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(state.items, key = { it.id }) { item ->
                WarrantyCard(
                    model = item,
                    onClick = onItemClick,
                    onDelete = onDelete,
                    onReminderToggle = onReminderToggle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
