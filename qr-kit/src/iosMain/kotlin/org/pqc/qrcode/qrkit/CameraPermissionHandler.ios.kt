package org.pqc.qrcode.qrkit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CompletableDeferred
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

class IOSCameraPermissionHandler : CameraPermissionHandler {
    override fun getStatus(): PermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            AVAuthorizationStatusDenied -> PermissionStatus.DENIED
            AVAuthorizationStatusRestricted -> PermissionStatus.DETERMINED_DENIED
            AVAuthorizationStatusNotDetermined -> PermissionStatus.NOT_DETERMINED
            else -> PermissionStatus.DENIED
        }
    }

    override suspend fun requestPermission(): PermissionStatus {
        val status = getStatus()
        if (status != PermissionStatus.NOT_DETERMINED) return status

        val deferred = CompletableDeferred<Boolean>()
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            deferred.complete(granted)
        }
        val granted = deferred.await()
        return if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
    }
}

@Composable
public actual fun rememberCameraPermissionHandler(): CameraPermissionHandler {
    return remember { IOSCameraPermissionHandler() }
}
