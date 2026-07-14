package org.pqc.qrcode.qrkit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Web Camera Scanner",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Camera streaming is native on Android & iOS. On Web, you can simulate scans or select files.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // Trigger a mock scan success for testing Web App integration
                    onQrCodeDetected("https://github.com/parallelquintillioncoders/qrcode")
                }
            ) {
                Text("Simulate Scan (Demo Link)")
            }
        }
    }
}
