package com.example.stockcount.data

/**
 * Simple in-memory repository providing sample data for export.
 */
object StockRepository {
    fun getItems(): List<StockItem> = listOf(
        StockItem(
            ean = "1234567890123",
            name = "Sample Item",
            quantity = 10,
            location = "Warehouse",
            note = "",
            updatedAt = "2024-01-01T00:00:00Z",
        ),
        StockItem(
            ean = "9876543210987",
            name = "Another Item",
            quantity = 5,
            location = "Storefront",
            note = "Damaged box",
            updatedAt = "2024-02-01T12:00:00Z",
        ),
    )
}
