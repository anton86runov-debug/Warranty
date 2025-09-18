package com.warranty.app.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.warranty.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWarrantyScreen(
    state: EditWarrantyState,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStoreChange: (String) -> Unit,
    onPurchaseDateChange: (LocalDate) -> Unit,
    onExpirationDateChange: (LocalDate?) -> Unit,
    onDurationMonthsChange: (String) -> Unit,
    onReminderChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onConsumeError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    LaunchedEffect(state.errorMessage) {
        val error = state.errorMessage
        if (!error.isNullOrBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar(error) }
            onConsumeError()
        }
    }

    var purchaseDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    var expirationDatePickerVisible by rememberSaveable { mutableStateOf(false) }

    if (purchaseDatePickerVisible) {
        val initialMillis = state.purchaseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { purchaseDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    purchaseDatePickerVisible = false
                    pickerState.selectedDateMillis?.let { millis ->
                        onPurchaseDateChange(millis.toLocalDate())
                    }
                }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseDatePickerVisible = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (expirationDatePickerVisible) {
        val initialMillis = state.expirationDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { expirationDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    expirationDatePickerVisible = false
                    pickerState.selectedDateMillis?.let { millis ->
                        onExpirationDateChange(millis.toLocalDate())
                    }
                }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    expirationDatePickerVisible = false
                    onExpirationDateChange(null)
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleRes = if (state.id == null) R.string.add_warranty else R.string.form_edit_title
                    Text(text = stringResource(id = titleRes))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(id = android.R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = onSave, enabled = !state.isSaving) {
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = stringResource(id = R.string.form_save))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text(text = stringResource(id = R.string.form_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.category,
                onValueChange = onCategoryChange,
                label = { Text(text = stringResource(id = R.string.form_category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.store,
                onValueChange = onStoreChange,
                label = { Text(text = stringResource(id = R.string.form_store)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.price,
                onValueChange = onPriceChange,
                label = { Text(text = stringResource(id = R.string.form_price)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = state.purchaseDate.toString(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { purchaseDatePickerVisible = true },
                label = { Text(text = stringResource(id = R.string.form_purchase_date)) }
            )

            OutlinedTextField(
                value = state.expirationDate?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expirationDatePickerVisible = true },
                label = { Text(text = stringResource(id = R.string.form_expiration_date)) }
            )

            OutlinedTextField(
                value = state.durationMonths,
                onValueChange = onDurationMonthsChange,
                label = { Text(text = stringResource(id = R.string.form_duration_months)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = R.string.reminder))
                Switch(checked = state.reminderEnabled, onCheckedChange = onReminderChange)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onSave, enabled = !state.isSaving) {
                Text(text = stringResource(id = R.string.form_save))
            }
        }
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
