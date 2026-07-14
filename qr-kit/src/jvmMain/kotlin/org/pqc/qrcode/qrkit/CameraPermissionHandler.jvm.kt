package org.pqc.qrcode.qrkit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class JvmCameraPermissionHandler : CameraPermissionHandler {
    override fun getStatus(): PermissionStatus {
        return PermissionStatus.GRANTED
    }

    override suspend fun requestPermission(): PermissionStatus {
        return PermissionStatus.GRANTED
    }
}

@Composable
public actual fun rememberCameraPermissionHandler(): CameraPermissionHandler {
    return remember { JvmCameraPermissionHandler() }
}
