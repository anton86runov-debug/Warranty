package com.warranty.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.warranty.app.R
import com.warranty.app.domain.model.WarrantyStatus
import com.warranty.app.ui.list.WarrantyUiModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WarrantyCard(
    model: WarrantyUiModel,
    onClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onReminderToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberCurrencyFormatter()
    val dateFormatter = rememberDateFormatter()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onClick(model.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!model.category.isNullOrBlank()) {
                        Text(
                            text = model.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (!model.store.isNullOrBlank()) {
                        Text(
                            text = model.store,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                IconButton(onClick = { onDelete(model.id) }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = statusLabel(model.status)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (model.status) {
                            WarrantyStatus.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            WarrantyStatus.EXPIRED -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            WarrantyStatus.ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        }
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = buildStatusSubtitle(model, dateFormatter))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (model.price != null) {
                    Text(text = currencyFormatter.format(model.price))
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onReminderToggle(model.id, !model.reminderEnabled) }) {
                        Text(text = stringResource(id = R.string.reminder))
                    }
                    Switch(
                        checked = model.reminderEnabled,
                        onCheckedChange = { onReminderToggle(model.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberCurrencyFormatter(): NumberFormat = remember {
    NumberFormat.getCurrencyInstance(Locale.getDefault())
}

@Composable
private fun rememberDateFormatter(): DateTimeFormatter = remember {
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
}

@Composable
private fun statusLabel(status: WarrantyStatus): String = when (status) {
    WarrantyStatus.ACTIVE -> stringResource(id = R.string.status_active)
    WarrantyStatus.EXPIRING_SOON -> stringResource(id = R.string.status_expiring)
    WarrantyStatus.EXPIRED -> stringResource(id = R.string.status_expired)
}

@Composable
private fun buildStatusSubtitle(
    model: WarrantyUiModel,
    formatter: DateTimeFormatter
): String {
    return when {
        model.daysRemaining == null -> stringResource(id = R.string.no_expiration)
        model.daysRemaining < 0 -> stringResource(
            id = R.string.expired_days,
            kotlin.math.abs(model.daysRemaining)
        )
        model.daysRemaining == 0L -> {
            val date = model.expirationDate
            if (date != null) stringResource(id = R.string.expires_on, formatter.format(date))
            else stringResource(id = R.string.expires_in_future, 0)
        }
        else -> stringResource(id = R.string.expires_in_future, model.daysRemaining)
    }
}
