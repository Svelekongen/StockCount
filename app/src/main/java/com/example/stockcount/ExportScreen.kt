package com.example.stockcount

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.stockcount.data.StockRepository
import com.example.stockcount.export.CsvExporter

/**
 * Screen offering a button to export repository data as CSV using SAF.
 */
@Composable
fun ExportScreen() {
    val context = LocalContext.current
    val items = remember { StockRepository.getItems() }
    val csv = remember(items) { CsvExporter.export(items) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { saveCsv(context, it, csv) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { launcher.launch("stock.csv") }) {
            Text("Export CSV")
        }
    }
}

private fun saveCsv(context: Context, uri: Uri, data: String) {
    context.contentResolver.openOutputStream(uri)?.use { out ->
        // Write BOM to hint Excel/Sheets at UTF-8 encoding
        out.write("\uFEFF".toByteArray(Charsets.UTF_8))
        out.write(data.toByteArray(Charsets.UTF_8))
    }
}
