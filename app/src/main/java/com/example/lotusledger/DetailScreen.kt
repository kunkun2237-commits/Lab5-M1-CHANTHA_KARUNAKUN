package com.example.lotusledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import java.time.format.DateTimeFormatter

// Finds the expense by id. If it is missing: "Expense not found" (no crash).
@Composable
fun DetailRoute(
    expenses: List<Expense>,
    expenseId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Expense) -> Unit
) {
    val current = expenses.firstOrNull { it.id == expenseId }

    // After a confirmed delete the expense is already gone from the list while the
    // screen slides out. Keep showing the last known values during that slide.
    val lastKnown = remember { arrayOfNulls<Expense>(1) }
    if (current != null) lastKnown[0] = current
    var leaving by remember { mutableStateOf(false) }
    val shown = current ?: if (leaving) lastKnown[0] else null

    if (shown == null) {
        NotFoundScreen(onBack = onBack)
    } else {
        DetailScreen(
            expense = shown,
            onBack = onBack,
            onEdit = onEdit,
            onDelete = {
                leaving = true
                onDelete(shown)
            }
        )
    }
}

// D6: hero amount on top + a "receipt" card of info rows below
@Composable
fun DetailScreen(
    expense: Expense,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    val dateText = expense.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.detail_title), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.spacing_l)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_l))
        ) {
            // hero amount
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_xl))) {
                    Text(
                        text = categoryLabel(expense.category),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_s)))
                    Text(
                        text = formatMoney(expense.amount, expense.currency),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // every field as an info row
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_l))) {
                    InfoRow(stringResource(R.string.field_currency), expense.currency)
                    HorizontalDivider()
                    InfoRow(stringResource(R.string.field_category), categoryLabel(expense.category))
                    HorizontalDivider()
                    InfoRow(stringResource(R.string.field_date), dateText)
                    HorizontalDivider()
                    InfoRow(
                        stringResource(R.string.field_remark),
                        expense.remark ?: stringResource(R.string.remark_none)
                    )
                }
            }

            // edit + delete
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_m))) {
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.edit))
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }

    // Delete asks first. Confirm removes it, Cancel changes nothing.
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_dialog_title)) },
            text = { Text(text = stringResource(R.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.spacing_m)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_m)))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
