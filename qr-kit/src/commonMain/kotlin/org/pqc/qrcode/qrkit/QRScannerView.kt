package org.pqc.qrcode.qrkit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

public enum class LensFacing {
    FRONT,
    BACK
}

public data class ScannerResolution(val width: Int, val height: Int)

@Composable
public expect fun QRScannerView(
    modifier: Modifier = Modifier,
    lensFacing: LensFacing = LensFacing.BACK,
    targetResolution: ScannerResolution? = null,
    flashlightEnabled: Boolean = false,
    scanWindowEnabled: Boolean = true,
    onQrCodeDetected: (String) -> Unit,
    onPermissionDenied: () -> Unit = {},
    onCameraError: (Throwable) -> Unit = {}
)
