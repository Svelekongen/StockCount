package com.example.stockcount.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import com.example.stockcount.data.CountRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    var lastScanAt by remember { mutableStateOf(0L) }
    val debounceMs = 2000L

    // Name prompt state when scanned item is missing a name
    var pendingEan by remember { mutableStateOf<String?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) }

    // Track last two scanned items for quick undo
    data class ScannedItem(val ean: String, val name: String?)
    var lastScans by remember { mutableStateOf<List<ScannedItem>>(emptyList()) }

    val repo = remember { CountRepository.get(context) }

    LaunchedEffect(hasPermission, torchEnabled) {
        cameraControl?.enableTorch(torchEnabled)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skann") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { 
                            Icon(Icons.Default.ArrowBack, contentDescription = null) 
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { torchEnabled = !torchEnabled }) {
                        Icon(if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (!hasPermission) {
            Column(Modifier.padding(padding).fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text("Tillat kameratilgang for å skanne.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Gi tilgang") }
            }
        } else {
            val previewView = remember { PreviewView(context) }
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    factory = { previewView }
                ) { view ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(view.surfaceProvider) }

                    val analysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val now = System.currentTimeMillis()
                                    val ean = barcodes.firstOrNull()?.rawValue
                                    if (ean != null && ean.isNotBlank() && (now - lastScanAt) >= debounceMs) {
                                        lastScanAt = now
                                        scope.launch {
                                            try {
                                                val existing = repo.getItem(ean)
                                                if (existing?.name.isNullOrBlank()) {
                                                    pendingEan = ean
                                                    nameInput = ""
                                                    showNameDialog = true
                                                } else {
                                                    repo.scan(ean)
                                                    val refreshed = repo.getItem(ean)
                                                    lastScans = (listOf(ScannedItem(ean = ean, name = refreshed?.name)) + lastScans).take(2)
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "+1 for $ean",
                                                        actionLabel = "Angre",
                                                        withDismissAction = true,
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        repo.undoScan(ean)
                                                    }
                                                }
                                            } catch (_: Throwable) {
                                                snackbarHostState.showSnackbar("Ugyldig strekkode")
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, analysis
                        )
                        cameraControl = camera.cameraControl
                        cameraControl?.enableTorch(torchEnabled)
                    } catch (_: Exception) { }
                }, ContextCompat.getMainExecutor(context))
                }

                Column(
                    modifier = Modifier.fillMaxWidth().weight(2f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Siste skanninger", style = MaterialTheme.typography.titleMedium)
                    lastScans.take(2).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.ean, style = MaterialTheme.typography.titleMedium)
                                item.name?.takeIf { it.isNotBlank() }?.let { n ->
                                    Text(n, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Button(onClick = {
                                scope.launch {
                                    repo.undoScan(item.ean)
                                    snackbarHostState.showSnackbar("Angret for ${'$'}{item.ean}")
                                }
                            }) { Text("Angre") }
                        }
                    }
                    if (lastScans.isEmpty()) {
                        Text("Ingen skanninger enda")
                    }
                }
            }
        }
    }

    // Dialog to capture name for barcodes without a name
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Legg til navn") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Strekkode: ${pendingEan ?: ""}")
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Navn") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ean = pendingEan
                        if (!nameInput.isBlank() && ean != null) {
                            scope.launch {
                                try {
                                    val current = repo.getItem(ean)
                                    if (current == null) {
                                        // Create with quantity = 1 and the provided name
                                        repo.save(
                                            com.example.stockcount.data.entity.CountLine(
                                                ean = ean,
                                                name = nameInput.trim(),
                                                quantity = 1,
                                                note = null,
                                                updatedAt = kotlinx.datetime.Clock.System.now()
                                            )
                                        )
                                    } else {
                                        // Update name and increment quantity
                                        repo.save(
                                            current.copy(
                                                name = nameInput.trim(),
                                                quantity = current.quantity + 1
                                            )
                                        )
                                    }
                                    lastScans = (listOf(ScannedItem(ean = ean, name = nameInput.trim())) + lastScans).take(2)
                                    showNameDialog = false
                                    pendingEan = null
                                    nameInput = ""
                                    snackbarHostState.showSnackbar("Navn lagret og +1 for $ean")
                                } catch (_: Throwable) {
                                    snackbarHostState.showSnackbar("Kunne ikke lagre navn")
                                }
                            }
                        }
                    }
                ) { Text("Lagre") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNameDialog = false
                        pendingEan = null
                        nameInput = ""
                    }
                ) { Text("Avbryt") }
            }
        )
    }
}


