package com.example.lotusledger

import java.util.Locale

// The fixed exchange rate, written ONCE (Part 5).
const val USD_TO_KHR = 4000.0

// Every amount is counted in KHR. A USD expense counts at USD_TO_KHR.
fun Expense.amountInKhr(): Double =
    if (currency == "USD") amount * USD_TO_KHR else amount

fun totalKhr(expenses: List<Expense>): Double = expenses.sumOf { it.amountInKhr() }

// Part 4: null means "All"
fun filterByCategory(expenses: List<Expense>, category: String?): List<Expense> =
    if (category == null) expenses else expenses.filter { it.category == category }

// (category, amount in KHR), biggest first
fun totalsByCategory(expenses: List<Expense>): List<Pair<String, Double>> =
    expenses
        .groupBy { it.category }
        .map { (category, items) -> category to totalKhr(items) }
        .sortedByDescending { it.second }

// newest first
fun recentFirst(expenses: List<Expense>): List<Expense> =
    expenses.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.id })

fun formatMoney(amount: Double, currency: String): String = when (currency) {
    "USD" -> "$" + String.format(Locale.US, "%,.2f", amount)
    "KHR" -> "៛" + String.format(Locale.US, "%,.0f", amount)
    else -> "$currency " + String.format(Locale.US, "%,.2f", amount)
}

fun formatKhr(amount: Double): String = formatMoney(amount, "KHR")
