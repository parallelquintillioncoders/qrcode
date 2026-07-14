package org.pqc.qrcode.qrkit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class WasmJsCameraPermissionHandler : CameraPermissionHandler {
    override fun getStatus(): PermissionStatus {
        return PermissionStatus.NOT_DETERMINED
    }

    override suspend fun requestPermission(): PermissionStatus {
        return PermissionStatus.GRANTED
    }
}

@Composable
public actual fun rememberCameraPermissionHandler(): CameraPermissionHandler {
    return remember { WasmJsCameraPermissionHandler() }
}
