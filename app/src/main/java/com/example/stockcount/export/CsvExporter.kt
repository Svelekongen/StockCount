package com.example.stockcount.export

import android.content.Context
import android.net.Uri
import com.example.stockcount.data.entity.CountLine
import java.io.OutputStreamWriter

object CsvExporter {
    fun write(context: Context, uri: Uri, lines: List<CountLine>, separator: Char = ',') {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                w.appendLine(listOf("ean","name","quantity","location","note","updated_at").joinToString(separator.toString()))
                lines.forEach { l ->
                    val cols = listOf(
                        quote(l.ean),
                        quote(l.name ?: ""),
                        quote(l.quantity.toString()),
                        quote(l.location ?: ""),
                        quote(l.note ?: ""),
                        quote(l.updatedAt.toString())
                    )
                    w.appendLine(cols.joinToString(separator.toString()))
                }
            }
        }
    }

    private fun quote(s: String): String = "\"${s.replace("\"", "\"\"")}\""
}


