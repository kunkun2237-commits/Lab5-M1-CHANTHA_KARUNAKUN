package com.example.lotusledger

import java.time.LocalDate

// Same fields and types as the lab asks (every later lab uses this class).
data class Expense(
    val id: String,           // UUID.randomUUID().toString()
    val amount: Double,
    val currency: String,     // "KHR" or "USD"
    val category: String,
    val date: LocalDate,
    val remark: String? = null
)
