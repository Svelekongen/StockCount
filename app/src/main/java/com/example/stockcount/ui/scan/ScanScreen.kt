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
    var lastScannedEan by remember { mutableStateOf<String?>(null) }
    val debounceMs = 500L

    val repo = remember { CountRepository.get(context) }

    LaunchedEffect(hasPermission, torchEnabled) {
        cameraControl?.enableTorch(torchEnabled)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Skann") }, navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
                }
            }, actions = {
                IconButton(onClick = { torchEnabled = !torchEnabled }) {
                    Icon(if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = null)
                }
            })
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
            AndroidView(
                modifier = Modifier.padding(padding).fillMaxSize(),
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
                        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
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
                                    if (ean != null && (now - lastScanAt) >= debounceMs && ean != lastScannedEan) {
                                        lastScanAt = now
                                        lastScannedEan = ean
                                        scope.launch {
                                            try {
                                                repo.scan(ean)
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "+1 for $ean",
                                                    actionLabel = "Angre",
                                                    withDismissAction = true,
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    repo.undoScan(ean)
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
        }
    }
}


