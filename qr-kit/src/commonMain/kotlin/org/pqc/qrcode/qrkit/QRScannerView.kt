package org.pqc.qrcode.qrkit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public expect fun QRScannerView(
    modifier: Modifier = Modifier,
    flashlightEnabled: Boolean = false,
    scanWindowEnabled: Boolean = true,
    onQrCodeDetected: (String) -> Unit,
    onPermissionDenied: () -> Unit = {}
)
