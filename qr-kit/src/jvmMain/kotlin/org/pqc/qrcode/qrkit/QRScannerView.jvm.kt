package org.pqc.qrcode.qrkit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamPanel
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.LuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

class BufferedImageLuminanceSource(private val image: BufferedImage) : LuminanceSource(image.width, image.height) {
    override fun getRow(y: Int, row: ByteArray?): ByteArray {
        val width = width
        val result = row ?: ByteArray(width)
        val pixels = image.getRGB(0, y, width, 1, null, 0, width)
        for (i in 0 until width) {
            val argb = pixels[i]
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            result[i] = ((r + g + b) / 3).toByte()
        }
        return result
    }

    override fun getMatrix(): ByteArray {
        val width = width
        val height = height
        val result = ByteArray(width * height)
        val pixels = image.getRGB(0, 0, width, height, null, 0, width)
        for (i in 0 until width * height) {
            val argb = pixels[i]
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            result[i] = ((r + g + b) / 3).toByte()
        }
        return result
    }
}

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
    val webcams = remember { 
        try {
            Webcam.getWebcams()
        } catch (e: Exception) {
            emptyList<Webcam>()
        }
    }
    
    val webcam = remember(lensFacing, webcams) {
        if (lensFacing == LensFacing.FRONT && webcams.size > 1) {
            webcams[1]
        } else {
            webcams.firstOrNull() ?: try { Webcam.getDefault() } catch (e: Exception) { null }
        }
    }

    val executor = remember { Executors.newSingleThreadScheduledExecutor() }

    DisposableEffect(webcam, targetResolution) {
        if (webcam != null) {
            if (!webcam.isOpen) {
                try {
                    val width = targetResolution?.width ?: 640
                    val height = targetResolution?.height ?: 480
                    // Webcam library requires matching one of supported sizes
                    val supportedSizes = webcam.viewSizes
                    val matchedSize = supportedSizes.firstOrNull { 
                        it.width == width && it.height == height 
                    } ?: supportedSizes.firstOrNull { it.width >= 640 } ?: supportedSizes.last()
                    
                    webcam.viewSize = matchedSize
                    webcam.open()
                } catch (e: Exception) {
                    try {
                        webcam.open()
                    } catch (ex: Exception) {
                        onCameraError(ex)
                    }
                }
            }

            val reader = MultiFormatReader()
            executor.scheduleAtFixedRate({
                if (webcam.isOpen) {
                    val image = webcam.image
                    if (image != null) {
                        try {
                            val source = BufferedImageLuminanceSource(image)
                            val bitmap = BinaryBitmap(HybridBinarizer(source))
                            val result = reader.decode(bitmap)
                            if (result != null && result.text != null) {
                                onQrCodeDetected(result.text)
                            }
                        } catch (e: Exception) {
                            // Decode failed (no QR code in frame)
                        }
                    }
                }
            }, 0, 150, TimeUnit.MILLISECONDS)
        }

        onDispose {
            executor.shutdown()
            try {
                webcam?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (webcam != null) {
        SwingPanel(
            modifier = modifier.fillMaxSize(),
            factory = {
                val panel = WebcamPanel(webcam, false)
                panel.isFPSDisplayed = false
                panel.isImageSizeDisplayed = false
                panel.isDisplayDebugInfo = false
                panel.start()
                panel
            },
            update = {}
        )
    } else {
        // Fallback placeholder if no camera is available
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("No camera detected or camera is busy")
        }
    }
}
