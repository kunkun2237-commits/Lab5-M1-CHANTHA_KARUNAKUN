package com.example.lotusledger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.util.UUID

// ONE state holder for the whole form (add and edit).
// original == null  -> adding a new expense
// original != null  -> editing that expense (the id stays the same)
class ExpenseFormState(
    val original: Expense?,
    val id: String,
    amountText: String,
    currency: String,
    category: String?,
    date: LocalDate,
    remark: String,
    amountTouched: Boolean = false,
    categoryTouched: Boolean = false,
    remarkTouched: Boolean = false
) {
    var amountText by mutableStateOf(amountText)
        private set
    var currency by mutableStateOf(currency)
        private set
    var category by mutableStateOf(category)
        private set
    var date by mutableStateOf(date)
        private set
    var remark by mutableStateOf(remark)
        private set

    // "touched" = the user has used that field. Errors show only after that.
    var amountTouched by mutableStateOf(amountTouched)
        private set
    var categoryTouched by mutableStateOf(categoryTouched)
        private set
    var remarkTouched by mutableStateOf(remarkTouched)
        private set

    // the values the form started with (to know if something changed)
    private val initialAmount = original?.let { amountToText(it.amount) } ?: ""
    private val initialCurrency = original?.currency ?: "KHR"
    private val initialCategory = original?.category
    private val initialDate = original?.date ?: LocalDate.now()
    private val initialRemark = original?.remark ?: ""

    val isEditing: Boolean get() = original != null

    val amountError: Boolean get() = amountTouched && !isAmountValid(amountText)
    val categoryError: Boolean get() = categoryTouched && category == null
    val remarkError: Boolean get() = remarkTouched && !isRemarkValid(remark)

    // Save is enabled only when EVERY rule passes
    val canSave: Boolean
        get() = isAmountValid(amountText) && category != null && isRemarkValid(remark)

    val hasChanges: Boolean
        get() = amountText != initialAmount ||
            currency != initialCurrency ||
            category != initialCategory ||
            date != initialDate ||
            remark != initialRemark

    fun onAmountChange(text: String) {
        amountText = cleanAmountInput(text)
        amountTouched = true
    }

    fun onCurrencyChange(code: String) {
        currency = code
    }

    // tapping the chosen chip again clears it (then "Choose a category" shows)
    fun onCategoryClick(key: String) {
        category = if (category == key) null else key
        categoryTouched = true
    }

    fun onDateChange(newDate: LocalDate) {
        date = newDate
    }

    fun onRemarkChange(text: String) {
        remark = text
        remarkTouched = true
    }

    // call only when canSave is true
    fun toExpense(): Expense = Expense(
        id = id,
        amount = amountText.toDouble(),
        currency = currency,
        category = category ?: "",
        date = date,
        remark = remark.trim().ifEmpty { null }
    )
}

private fun newFormState(original: Expense?): ExpenseFormState =
    if (original == null) {
        // new expense: new id, KHR, empty, today
        ExpenseFormState(
            original = null,
            id = UUID.randomUUID().toString(),
            amountText = "",
            currency = "KHR",
            category = null,
            date = LocalDate.now(),
            remark = ""
        )
    } else {
        ExpenseFormState(
            original = original,
            id = original.id,
            amountText = amountToText(original.amount),
            currency = original.currency,
            category = original.category,
            date = original.date,
            remark = original.remark ?: ""
        )
    }

// Saves the typed values so a rotation does not clear the form.
private fun formStateSaver(original: Expense?): Saver<ExpenseFormState, Any> =
    listSaver<ExpenseFormState, Any?>(
        save = { s ->
            listOf(
                s.id, s.amountText, s.currency, s.category, s.date.toEpochDay(), s.remark,
                s.amountTouched, s.categoryTouched, s.remarkTouched
            )
        },
        restore = { l ->
            ExpenseFormState(
                original = original,
                id = l[0] as String,
                amountText = l[1] as String,
                currency = l[2] as String,
                category = l[3] as String?,
                date = LocalDate.ofEpochDay(l[4] as Long),
                remark = l[5] as String,
                amountTouched = l[6] as Boolean,
                categoryTouched = l[7] as Boolean,
                remarkTouched = l[8] as Boolean
            )
        }
    )

@Composable
fun rememberExpenseFormState(original: Expense?): ExpenseFormState =
    rememberSaveable(original?.id, saver = formStateSaver(original)) {
        newFormState(original)
    }
