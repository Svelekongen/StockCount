package com.example.stockcount.ui.export

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockcount.export.CsvExporter
import com.example.stockcount.settings.UserSettings

@Composable
fun ExportScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: ExportViewModel = viewModel { ExportViewModel(context.applicationContext as android.app.Application) }
    val items by viewModel.items.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedSeparator by remember { mutableStateOf(settings.csvSeparator) }
    var isExporting by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isExporting = false
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    CsvExporter.write(context, uri, items, selectedSeparator)
                    showExportDialog = true
                } catch (e: Exception) {
                    // Handle export error
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eksporter til CSV") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Eksportoversikt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Antall varer: ${items.size}")
                    Text("Totalt antall: ${items.sumOf { it.quantity }}")
                }
            }

            // CSV Separator selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "CSV-separator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { 
                                selectedSeparator = ','
                                viewModel.updateSeparator(',')
                            },
                            label = { Text("Komma (,)") },
                            selected = selectedSeparator == ','
                        )
                        FilterChip(
                            onClick = { 
                                selectedSeparator = ';'
                                viewModel.updateSeparator(';')
                            },
                            label = { Text("Semikolon (;)") },
                            selected = selectedSeparator == ';'
                        )
                    }
                }
            }

            // Export button
            Button(
                onClick = {
                    if (items.isNotEmpty()) {
                        isExporting = true
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TITLE, "stockcount_${System.currentTimeMillis()}.csv")
                        }
                        exportLauncher.launch(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = items.isNotEmpty() && !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isExporting) "Eksporterer..." else "Eksporter til CSV")
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingen data å eksportere.\nSkanne noen varer først.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Export success dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Eksport fullført") },
            text = { Text("CSV-filen er lagret. Du kan nå åpne den i Excel eller Google Sheets.") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
