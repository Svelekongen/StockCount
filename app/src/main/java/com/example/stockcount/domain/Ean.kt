package com.example.stockcount.domain

object Ean13 {
    fun isValid(ean: String): Boolean {
        if (ean.length != 13 || !ean.all { it.isDigit() }) return false
        val digits = ean.map { it - '0' }
        val sum = digits.take(12).mapIndexed { idx, d -> if ((idx + 1) % 2 == 0) d * 3 else d }.sum()
        val check = (10 - (sum % 10)) % 10
        return check == digits[12]
    }
}


