package com.example.stockcount.import

import android.content.Context
import android.net.Uri
import com.example.stockcount.data.CountRepository
import com.example.stockcount.data.entity.CountLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object CatalogImporter {
    /**
     * Imports a mapping of EAN -> Name from a CSV file (first column = barcode, second = name).
     * Existing quantities are preserved. Rows with empty barcode are ignored.
     */
    suspend fun importCsv(context: Context, uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val repo = CountRepository.get(context)
        var processed = 0
        var updated = 0
        var created = 0

        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEach
                    // Very simple CSV split supporting comma or semicolon; basic quote handling
                    val columns = splitCsvLine(trimmed)
                    if (columns.isEmpty()) return@forEach
                    val ean = columns.getOrNull(0)?.trim().orEmpty()
                    if (ean.isBlank()) return@forEach
                    val name = columns.getOrNull(1)?.trim().orEmpty()
                    processed++

                    // Preserve existing quantity and note if present
                    val existing = repo.getItem(ean)
                    val now = kotlinx.datetime.Clock.System.now()
                    if (existing == null) {
                        repo.save(
                            CountLine(
                                ean = ean,
                                name = name.ifBlank { null },
                                quantity = 0,
                                note = null,
                                updatedAt = now
                            )
                        )
                        created++
                    } else {
                        val newName = name.ifBlank { existing.name }
                        if (newName != existing.name) {
                            repo.save(
                                existing.copy(
                                    name = newName,
                                    updatedAt = now
                                )
                            )
                            updated++
                        }
                    }
                }
            }
        }

        ImportResult(processed = processed, created = created, updated = updated)
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when (c) {
                '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ',', ';' -> {
                    if (inQuotes) sb.append(c) else {
                        result.add(sb.toString())
                        sb.setLength(0)
                    }
                }
                else -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }

    data class ImportResult(
        val processed: Int,
        val created: Int,
        val updated: Int
    )
}


