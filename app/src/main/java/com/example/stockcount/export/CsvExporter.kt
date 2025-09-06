package com.example.stockcount.export

import com.example.stockcount.data.StockItem

/**
 * Builds a CSV representation of stock items using comma delimiters and UTF-8 encoding.
 */
object CsvExporter {
    private val headers = listOf("ean", "name", "quantity", "location", "note", "updated_at")

    fun export(items: List<StockItem>): String {
        val sb = StringBuilder()
        sb.append(headers.joinToString(",")).append('\n')
        for (item in items) {
            sb.append(
                listOf(
                    item.ean,
                    item.name,
                    item.quantity.toString(),
                    item.location,
                    item.note,
                    item.updatedAt,
                ).joinToString(",") { escape(it) }
            ).append('\n')
        }
        return sb.toString()
    }

    private fun escape(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('\n') || value.contains('"')
        var result = value.replace("\"", "\"\"")
        if (needsQuotes) {
            result = "\"$result\""
        }
        return result
    }
}
