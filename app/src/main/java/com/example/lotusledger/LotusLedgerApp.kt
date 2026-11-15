package com.example.lotusledger

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private const val TAG = "LotusLedger"

// ---------------- Part 1: one NavKey per screen ----------------
@Serializable
data object Dashboard : NavKey

@Serializable
data class Detail(val expenseId: String) : NavKey

@Serializable
data class Form(val expenseId: String? = null) : NavKey   // null = add, id = edit

// The CALLER: owns the state (list, filter, back stack) and handles every event.
@Composable
fun LotusLedgerApp() {
    val expenses = remember { mutableStateListOf<Expense>().apply { addAll(sampleExpenses) } }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val backStack = rememberNavBackStack(Dashboard)

    // Part 4: the chosen category survives a rotation
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val deletedMessage = stringResource(R.string.expense_deleted)
    val savedMessage = stringResource(R.string.expense_saved)
    val undoLabel = stringResource(R.string.undo)

    // Delete + "Undo" snackbar (from Lab 4)
    fun deleteExpense(expense: Expense) {
        val index = expenses.indexOf(expense)
        if (index < 0) return
        expenses.removeAt(index)
        Log.d(TAG, "Deleted: ${expense.category} ${expense.amount} ${expense.currency} (id ${expense.id})")

        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                expenses.add(index.coerceAtMost(expenses.size), expense)
                Log.d(TAG, "Undo delete: ${expense.category} ${expense.amount} (id ${expense.id})")
            }
        }
    }

    // Save from the Form: a new id adds, an existing id replaces
    fun saveExpense(expense: Expense, isEdit: Boolean) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index >= 0) {
            expenses[index] = expense
        } else {
            expenses.add(expense)
        }
        Log.d(TAG, (if (isEdit) "Edited: " else "Added: ") +
            "${expense.category} ${expense.amount} ${expense.currency} (id ${expense.id})")

        backStack.removeLastOrNull()                                  // close the Form
        if (isEdit && backStack.lastOrNull() is Detail) {
            backStack.removeLastOrNull()                              // and the old Detail: Back never returns to it
        }
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = savedMessage)
        }
    }

    // ---------------- Part 1: NavDisplay ----------------
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },       // phone Back
        // slides in from the right, slides out to the right
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) togetherWith
                slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(300))
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) togetherWith
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) togetherWith
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
        },
        entryProvider = entryProvider {
            entry<Dashboard> {
                DashboardScreen(
                    expenses = expenses,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    snackbarHostState = snackbarHostState,
                    // the add button opens an EMPTY form
                    onAddClick = {
                        if (backStack.lastOrNull() == Dashboard) backStack.add(Form())
                    },
                    onExpenseClick = { expense ->
                        if (backStack.lastOrNull() == Dashboard) backStack.add(Detail(expense.id))
                    },
                    onDelete = { deleteExpense(it) }
                )
            }

            entry<Detail> { key ->
                DetailRoute(
                    expenses = expenses,
                    expenseId = key.expenseId,
                    onBack = { backStack.removeLastOrNull() },
                    onEdit = {
                        if (backStack.lastOrNull() == key) backStack.add(Form(key.expenseId))
                    },
                    onDelete = { expense ->
                        deleteExpense(expense)
                        backStack.removeLastOrNull()      // back to the Dashboard
                    }
                )
            }

            entry<Form> { key ->
                FormRoute(
                    expenses = expenses,
                    expenseId = key.expenseId,
                    onClose = { backStack.removeLastOrNull() },
                    onSave = { expense, isEdit -> saveExpense(expense, isEdit) }
                )
            }
        }
    )
}
