package com.example.stockcount.export

import com.example.stockcount.data.StockItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {
    @Test
    fun `exports CSV with header and rows`() {
        val items = listOf(
            StockItem("1", "Name", 2, "Loc", "Note", "2024-01-01"),
        )
        val csv = CsvExporter.export(items)
        val expected = "ean,name,quantity,location,note,updated_at\n1,Name,2,Loc,Note,2024-01-01\n"
        assertEquals(expected, csv)
        // Verify UTF-8 encoding
        val bytes = csv.toByteArray(Charsets.UTF_8)
        assertEquals(csv, String(bytes, Charsets.UTF_8))
    }
}
