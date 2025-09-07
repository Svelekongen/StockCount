package com.example.stockcount.settings

data class UserSettings(
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val csvSeparator: Char = ',',
    val flashBehavior: FlashBehavior = FlashBehavior.MANUAL
) {
    enum class FlashBehavior { AUTO, MANUAL }
}


