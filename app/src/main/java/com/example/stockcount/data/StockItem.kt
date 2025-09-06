package com.example.stockcount.data

/**
 * Represents a single stock entry that can be exported to CSV.
 */
data class StockItem(
    val ean: String,
    val name: String,
    val quantity: Int,
    val location: String,
    val note: String,
    val updatedAt: String,
)
