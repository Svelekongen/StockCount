package com.example.stockcount.ui.import

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.stockcount.import.CatalogImporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isImporting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = res.data?.data
            if (uri != null) {
                isImporting = true
                resultMessage = null
                // Launch import
                LaunchedEffect(uri) {
                    try {
                        val result = CatalogImporter.importCsv(context, uri)
                        resultMessage = "Importert: prosessert ${'$'}{result.processed}, nye ${'$'}{result.created}, oppdatert ${'$'}{result.updated}"
                    } catch (e: Exception) {
                        resultMessage = "Import feilet: ${'$'}{e.message ?: "ukjent feil"}"
                    } finally {
                        isImporting = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importer katalog") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Velg en CSV-fil med strekkode i kolonne 1 og navn i kolonne 2.")
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    }
                    pickLauncher.launch(intent)
                },
                enabled = !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isImporting) "Importerer..." else "Velg fil for import")
            }

            resultMessage?.let { Text(it) }
        }
    }
}


