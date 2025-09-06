package com.example.stockcount.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.stockcount.data.ScanRepository
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalGetImage::class)
private class Ean13Analyzer(
    private val onScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        val media = imageProxy.image
        if (media != null) {
            val image = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val code = barcodes.firstOrNull()?.rawValue
                    if (code != null && isValidEan13(code)) {
                        onScanned(code)
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}

fun isValidEan13(code: String): Boolean {
    if (!code.matches(Regex("\\d{13}"))) return false
    val digits = code.map { it - '0' }
    val sum = digits.take(12).mapIndexed { index, d -> if (index % 2 == 0) d else d * 3 }.sum()
    val check = (10 - sum % 10) % 10
    return check == digits[12]
}

@Composable
fun ScanScreen(repository: ScanRepository) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS or CameraController.PREVIEW)
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            controller.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                Ean13Analyzer { code ->
                    repository.add(code)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Scanned $code",
                            actionLabel = "Undo"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            repository.remove(code)
                        }
                    }
                }
            )
            controller.bindToLifecycle(lifecycleOwner)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (hasPermission) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        this.controller = controller
                    }
                }
            )
        } else {
            Box(modifier = Modifier.padding(padding)) {
                Text("Camera permission required")
            }
        }
    }
}
