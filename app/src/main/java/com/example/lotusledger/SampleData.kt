package com.example.lotusledger

import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

// Starting data (replace with your own expenses from Lab 3 / Lab 4).

private fun thisMonth(day: Int): LocalDate {
    val today = LocalDate.now()
    return YearMonth.from(today).atDay(day.coerceAtMost(today.dayOfMonth))
}

private fun lastMonth(day: Int): LocalDate =
    YearMonth.from(LocalDate.now()).minusMonths(1).atDay(day)

private fun id(): String = UUID.randomUUID().toString()

val sampleExpenses: List<Expense> = listOf(
    Expense(id(), 8000.0, "KHR", "Food", thisMonth(2), "Lunch near campus"),
    Expense(id(), 5000.0, "KHR", "Transport", thisMonth(1), "Tuk-tuk"),
    Expense(id(), 3.0, "USD", "Bills", thisMonth(3), "Phone top-up"),
    Expense(id(), 4.5, "USD", "Shopping", thisMonth(5), "USB cable"),
    Expense(id(), 6.0, "USD", "Fun", thisMonth(6), "Movie ticket"),
    Expense(id(), 6000.0, "KHR", "Food", thisMonth(8)),
    Expense(id(), 2000.0, "KHR", "Transport", thisMonth(9), "Bus"),
    Expense(id(), 6.25, "USD", "Study", thisMonth(12), "Textbook"),
    Expense(id(), 9.8, "USD", "Food", thisMonth(14), "Dinner with friends"),
    Expense(id(), 12.0, "USD", "Bills", thisMonth(15), "Internet"),
    Expense(id(), 20.0, "USD", "Bills", lastMonth(28), "Electricity"),
    Expense(id(), 32000.0, "KHR", "Food", lastMonth(25), "Groceries")
)

// My own categories (the key is stored in the data, the label comes from strings.xml)
val categories: List<String> = listOf("Food", "Transport", "Shopping", "Bills", "Fun", "Study")
