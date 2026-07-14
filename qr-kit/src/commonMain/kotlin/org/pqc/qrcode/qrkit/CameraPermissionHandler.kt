package org.pqc.qrcode.qrkit

import androidx.compose.runtime.Composable

public enum class PermissionStatus {
    GRANTED,
    DENIED,
    DETERMINED_DENIED, // Permanently denied, requires manually opening app settings
    NOT_DETERMINED
}

public interface CameraPermissionHandler {
    public fun getStatus(): PermissionStatus
    public suspend fun requestPermission(): PermissionStatus
}

@Composable
public expect fun rememberCameraPermissionHandler(): CameraPermissionHandler
