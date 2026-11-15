package com.example.lotusledger

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.lotusledger.ui.theme.LotusLedgerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

// ---------------------------------------------------------------
// STATELESS screen: gets the list, the filter and the events.
// The state lives in the caller (LotusLedgerApp).
// ---------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenses: List<Expense>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    // Part 4: plain vals worked out from the list and the filter (no second state)
    val shown = recentFirst(filterByCategory(expenses, selectedCategory))
    val total = totalKhr(shown)                       // follows the filter
    val byCategory = totalsByCategory(expenses)       // overview of everything

    // The empty flag changes much less often than the list: derivedStateOf
    val isEmpty by remember(expenses, selectedCategory) {
        derivedStateOf { filterByCategory(expenses, selectedCategory).isEmpty() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // D5: floating action button, now opens the Form
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_expense)
                )
            }
        }
    ) { innerPadding ->
        RecentExpensesList(
            expenses = shown,
            isEmpty = isEmpty,
            onExpenseClick = onExpenseClick,
            onDelete = onDelete,
            modifier = Modifier.padding(innerPadding),
            header = {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_l))) {
                    TotalCard(total = total)
                    CategorySection(totals = byCategory)
                    CategoryFilter(selected = selectedCategory, onSelected = onCategorySelected)
                }
            }
        )
    }
}

// ---------------------------------------------------------------
// Total in KHR. The number counts up to its new value in about half a second.
// If "Remove animations" is on in the phone, Compose skips the animation by itself.
// ---------------------------------------------------------------
@Composable
fun TotalCard(total: Double, modifier: Modifier = Modifier) {
    val animatedTotal by animateFloatAsState(
        targetValue = total.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "total"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_xl))) {
            Text(
                text = stringResource(R.string.total_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_s)))
            Text(
                text = formatKhr(animatedTotal.roundToLong().toDouble()),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ---------------------------------------------------------------
// Spending by category (horizontal bars, amounts in KHR)
// ---------------------------------------------------------------
@Composable
fun CategorySection(totals: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val biggest = totals.maxOfOrNull { it.second } ?: 1.0

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_l))) {
            Text(
                text = stringResource(R.string.category_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_m)))

            totals.forEach { (category, amount) ->
                val color = categoryColor(category)
                val fraction = (amount / biggest).toFloat().coerceIn(0.03f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = categoryLabel(category), style = MaterialTheme.typography.bodyMedium)
                    Text(text = formatKhr(amount), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xs)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.bar_height))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.bar_radius)))
                        .background(color.copy(alpha = 0.18f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.bar_radius)))
                            .background(color)
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_m)))
            }
        }
    }
}

// ---------------------------------------------------------------
// D9: category filter as a dropdown. null = "All".
// ---------------------------------------------------------------
@Composable
fun CategoryFilter(
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.filter_label),
            style = MaterialTheme.typography.titleMedium
        )
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(
                    text = if (selected == null) {
                        stringResource(R.string.filter_all)
                    } else {
                        categoryLabel(selected)
                    }
                )
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.filter_all)) },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    }
                )
                categories.forEach { key ->
                    DropdownMenuItem(
                        text = { Text(text = categoryLabel(key)) },
                        onClick = {
                            onSelected(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// Recent expenses (LazyColumn). Empty message replaces the rows.
// ---------------------------------------------------------------
@Composable
fun RecentExpensesList(
    expenses: List<Expense>,
    isEmpty: Boolean,
    onExpenseClick: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {}
) {
    val padding = dimensionResource(R.dimen.spacing_l)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = padding,
            end = padding,
            top = padding,
            bottom = dimensionResource(R.dimen.fab_clearance)
        )
    ) {
        item {
            header()
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xl)))
        }
        item {
            Text(
                text = stringResource(R.string.recent_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_xs))
            )
        }
        if (isEmpty) {
            item { EmptyMessage() }
        } else {
            items(items = expenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    onClick = { onExpenseClick(expense) },
                    onDelete = { onDelete(expense) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun EmptyMessage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_xl)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.empty_message),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_xs)))
            Text(
                text = stringResource(R.string.empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------
// One row: tap it to open the Detail screen. Trash button at the end.
// ---------------------------------------------------------------
@Composable
fun ExpenseRow(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val dateText = expense.date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
    val secondLine = expense.remark?.let {
        stringResource(R.string.date_and_remark, dateText, it)
    } ?: dateText
    val label = categoryLabel(expense.category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = dimensionResource(R.dimen.spacing_xs)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.badge_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_m)))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = secondLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatMoney(expense.amount, expense.currency),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_expense),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Category name shown to the user comes from strings.xml (so it can be Khmer).
@Composable
fun categoryLabel(category: String): String = when (category) {
    "Food" -> stringResource(R.string.cat_food)
    "Transport" -> stringResource(R.string.cat_transport)
    "Shopping" -> stringResource(R.string.cat_shopping)
    "Bills" -> stringResource(R.string.cat_bills)
    "Fun" -> stringResource(R.string.cat_fun)
    "Study" -> stringResource(R.string.cat_study)
    else -> category
}

// Bar colours come from theme roles only (no colour is written here).
@Composable
fun categoryColor(category: String): Color {
    val scheme = MaterialTheme.colorScheme
    return when (category) {
        "Food" -> scheme.primary
        "Transport" -> scheme.secondary
        "Shopping" -> scheme.tertiary
        "Bills" -> scheme.outline
        "Fun" -> scheme.inversePrimary
        "Study" -> scheme.error
        else -> scheme.onSurfaceVariant
    }
}

// ---------------------------------------------------------------
// Previews
// ---------------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardLightPreview() {
    LotusLedgerTheme(darkTheme = false, dynamicColor = false) {
        DashboardScreen(sampleExpenses, null, {}, remember { SnackbarHostState() }, {}, {}, {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DashboardDarkPreview() {
    LotusLedgerTheme(darkTheme = true, dynamicColor = false) {
        DashboardScreen(sampleExpenses, "Food", {}, remember { SnackbarHostState() }, {}, {}, {})
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "km")
@Composable
fun DashboardKhmerPreview() {
    LotusLedgerTheme(darkTheme = false, dynamicColor = false) {
        DashboardScreen(sampleExpenses, null, {}, remember { SnackbarHostState() }, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseRowPreview() {
    LotusLedgerTheme(darkTheme = false, dynamicColor = false) {
        ExpenseRow(
            expense = Expense("preview", 8000.0, "KHR", "Food", LocalDate.now(), "Lunch near campus"),
            onClick = {},
            onDelete = {}
        )
    }
}
