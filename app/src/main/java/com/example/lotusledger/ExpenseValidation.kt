package com.example.lotusledger

import java.math.BigDecimal

// Form rules as plain functions (no Compose), so they are easy to test.

const val MAX_REMARK = 60

// Keeps digits and ONE dot only (number keyboard only)
fun cleanAmountInput(text: String): String {
    val out = StringBuilder()
    var seenDot = false
    for (ch in text) {
        if (ch in '0'..'9') {
            out.append(ch)
        } else if (ch == '.' && !seenDot) {
            seenDot = true
            out.append(ch)
        }
    }
    return out.toString().take(12)
}

fun parseAmount(text: String): Double? = text.trim().toDoubleOrNull()

// Amount: more than 0
fun isAmountValid(text: String): Boolean {
    val value = parseAmount(text) ?: return false
    return value > 0.0
}

// Remark: optional, at most 60 characters
fun isRemarkValid(text: String): Boolean = text.length <= MAX_REMARK

// 20000.0 -> "20000", 4.5 -> "4.5" (for the edit form)
fun amountToText(amount: Double): String =
    BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
