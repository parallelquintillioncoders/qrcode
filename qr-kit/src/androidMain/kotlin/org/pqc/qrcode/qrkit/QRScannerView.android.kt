package org.pqc.qrcode.qrkit

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
public actual fun QRScannerView(
    modifier: Modifier,
    lensFacing: LensFacing,
    targetResolution: ScannerResolution?,
    flashlightEnabled: Boolean,
    scanWindowEnabled: Boolean,
    onQrCodeDetected: (String) -> Unit,
    onPermissionDenied: () -> Unit,
    onCameraError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    val previewView = remember { PreviewView(context) }
    
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    var cameraControl: androidx.camera.core.CameraControl? = null

    LaunchedEffect(flashlightEnabled) {
        cameraControl?.enableTorch(flashlightEnabled)
    }

    DisposableEffect(lensFacing, targetResolution) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val previewBuilder = Preview.Builder()
            val analysisBuilder = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                
            if (targetResolution != null) {
                @Suppress("DEPRECATION")
                previewBuilder.setTargetResolution(android.util.Size(targetResolution.width, targetResolution.height))
                @Suppress("DEPRECATION")
                analysisBuilder.setTargetResolution(android.util.Size(targetResolution.width, targetResolution.height))
            }
            
            val preview = previewBuilder.build().apply {
                surfaceProvider = previewView.surfaceProvider
            }
            
            val imageAnalysis = analysisBuilder.build()
                
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { qrValue ->
                                    onQrCodeDetected(qrValue)
                                }
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }
            
            val cameraSelector = if (lensFacing == LensFacing.FRONT) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                cameraControl = camera.cameraControl
                cameraControl?.enableTorch(flashlightEnabled)
            } catch (e: Exception) {
                onCameraError(e)
            }
        }, ContextCompat.getMainExecutor(context))
        
        onDispose {
            cameraExecutor.shutdown()
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
