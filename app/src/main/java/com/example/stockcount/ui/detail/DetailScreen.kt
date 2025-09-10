package com.example.stockcount.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockcount.data.entity.CountLine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    ean: String,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: DetailViewModel = viewModel { DetailViewModel(context.applicationContext as android.app.Application) }
    val scope = rememberCoroutineScope()
    
    var item by remember { mutableStateOf<CountLine?>(null) }
    var quantity by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Load item data
    LaunchedEffect(ean) {
        viewModel.loadItem(ean) { loadedItem ->
            item = loadedItem
            quantity = loadedItem?.quantity?.toString() ?: "0"
            name = loadedItem?.name ?: ""
            location = loadedItem?.location ?: ""
            note = loadedItem?.note ?: ""
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rediger vare") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showSaveDialog = true },
                        enabled = !isLoading
                    ) {
                        Text("Lagre")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (item == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Vare ikke funnet")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // EAN (read-only)
                OutlinedTextField(
                    value = ean,
                    onValueChange = { },
                    label = { Text("EAN") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true
                )

                // Quantity
                Text(
                    text = "Antall",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val current = quantity.toIntOrNull() ?: 0
                            quantity = (current - 1).coerceAtLeast(0).toString()
                        }
                    ) {
                        Text("-", style = MaterialTheme.typography.headlineMedium)
                    }
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                quantity = newValue
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            val current = quantity.toIntOrNull() ?: 0
                            quantity = (current + 1).toString()
                        }
                    ) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Navn (valgfritt)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasjon (valgfritt)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notat (valgfritt)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
    }

    // Save confirmation dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Lagre endringer") },
            text = { Text("Er du sikker på at du vil lagre endringene?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = quantity.toIntOrNull() ?: 0
                        if (qty >= 0) {
                            val updatedItem = item?.copy(
                                quantity = qty,
                                name = name.takeIf { it.isNotBlank() },
                                location = location.takeIf { it.isNotBlank() },
                                note = note.takeIf { it.isNotBlank() }
                            )
                            if (updatedItem != null) {
                                scope.launch {
                                    viewModel.save(updatedItem)
                                    onBack?.invoke()
                                }
                            }
                        }
                        showSaveDialog = false
                    }
                ) {
                    Text("Lagre")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }
}
