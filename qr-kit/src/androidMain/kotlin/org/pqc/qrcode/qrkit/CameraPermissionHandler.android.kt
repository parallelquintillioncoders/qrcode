package org.pqc.qrcode.qrkit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

class AndroidCameraPermissionHandler(
    private val context: Context,
    private val requestLauncher: (CompletableDeferred<Boolean>) -> Unit
) : CameraPermissionHandler {

    override fun getStatus(): PermissionStatus {
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }

    override suspend fun requestPermission(): PermissionStatus {
        if (getStatus() == PermissionStatus.GRANTED) return PermissionStatus.GRANTED
        
        val deferred = CompletableDeferred<Boolean>()
        requestLauncher(deferred)
        val success = deferred.await()
        return if (success) PermissionStatus.GRANTED else PermissionStatus.DENIED
    }
}

@Composable
public actual fun rememberCameraPermissionHandler(): CameraPermissionHandler {
    val context = LocalContext.current
    var currentDeferred: CompletableDeferred<Boolean>? = null
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        currentDeferred?.complete(isGranted)
    }
    
    return remember(context) {
        AndroidCameraPermissionHandler(context) { deferred ->
            currentDeferred = deferred
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
}
