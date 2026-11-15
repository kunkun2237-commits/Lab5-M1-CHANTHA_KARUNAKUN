package com.example.lotusledger

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Finds the expense to edit (if any) and builds the ONE form state holder.
@Composable
fun FormRoute(
    expenses: List<Expense>,
    expenseId: String?,
    onClose: () -> Unit,
    onSave: (expense: Expense, isEdit: Boolean) -> Unit
) {
    // looked up once when the form opens
    val original = remember(expenseId) {
        expenseId?.let { id -> expenses.firstOrNull { it.id == id } }
    }

    if (expenseId != null && original == null) {
        NotFoundScreen(onBack = onClose)
    } else {
        val state = rememberExpenseFormState(original)
        FormScreen(
            state = state,
            onClose = onClose,
            onSave = { expense -> onSave(expense, state.isEditing) }
        )
    }
}

// D7 = chip row for the category.  D8 = big Save button at the bottom.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FormScreen(
    state: ExpenseFormState,
    onClose: () -> Unit,
    onSave: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]

    // Back (phone Back and the arrow): with changes ask first, without changes close at once
    val requestBack: () -> Unit = {
        if (state.hasChanges) {
            showDiscardDialog = true
        } else {
            onClose()
        }
    }
    BackHandler(enabled = state.hasChanges) { showDiscardDialog = true }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(
                    if (state.isEditing) R.string.form_title_edit else R.string.form_title_add
                ),
                onBack = requestBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(dimensionResource(R.dimen.spacing_l)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_l))
            ) {
                // ---- Amount (number keyboard) + currency next to it ----
                Row(verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = state.amountText,
                        onValueChange = { state.onAmountChange(it) },
                        label = { Text(text = stringResource(R.string.field_amount)) },
                        singleLine = true,
                        isError = state.amountError,
                        supportingText = {
                            FieldError(
                                visible = state.amountError,
                                message = stringResource(R.string.error_amount)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_s)))
                    CurrencyToggle(
                        selected = state.currency,
                        onSelected = { state.onCurrencyChange(it) },
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_s))
                    )
                }

                // ---- Category (D7: chip row) ----
                Column {
                    Text(
                        text = stringResource(R.string.field_category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_s)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xs))
                    ) {
                        categories.forEach { key ->
                            FilterChip(
                                selected = state.category == key,
                                onClick = { state.onCategoryClick(key) },
                                label = { Text(text = categoryLabel(key)) }
                            )
                        }
                    }
                    FieldError(
                        visible = state.categoryError,
                        message = stringResource(R.string.error_category)
                    )
                }

                // ---- Date (from a calendar, never typed) ----
                Column {
                    Text(
                        text = stringResource(R.string.field_date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.spacing_l)),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.date.format(
                                    DateTimeFormatter.ofPattern("d MMM yyyy", locale)
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = stringResource(R.string.pick_date)
                            )
                        }
                    }
                }

                // ---- Remark (optional, at most 60 characters) ----
                OutlinedTextField(
                    value = state.remark,
                    onValueChange = { state.onRemarkChange(it) },
                    label = { Text(text = stringResource(R.string.field_remark_optional)) },
                    minLines = 2,
                    maxLines = 3,
                    isError = state.remarkError,
                    supportingText = {
                        FieldError(
                            visible = state.remarkError,
                            message = stringResource(R.string.error_remark)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // D8: Save is a big button at the bottom, gray until every rule passes
            Button(
                onClick = { onSave(state.toExpense()) },
                enabled = state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_l))
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }

    // ---- Material date picker ----
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        state.onDateChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showDatePicker = false
                }) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // ---- Discard changes? ----
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(text = stringResource(R.string.discard_title)) },
            text = { Text(text = stringResource(R.string.discard_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onClose()
                }) {
                    Text(text = stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(text = stringResource(R.string.keep_editing))
                }
            }
        )
    }
}

// KHR / USD next to the amount (starts at KHR)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyToggle(selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf("KHR" to R.string.currency_khr, "USD" to R.string.currency_usd)
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option.first,
                onClick = { onSelected(option.first) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(text = stringResource(option.second))
            }
        }
    }
}
