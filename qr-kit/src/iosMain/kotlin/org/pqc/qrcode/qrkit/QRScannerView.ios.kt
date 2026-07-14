package org.pqc.qrcode.qrkit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.darwin.dispatch_get_main_queue
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject

class QRMetadataDelegate(
    private val onQrCodeDetected: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        for (metadataObject in didOutputMetadataObjects) {
            val readableObject = metadataObject as? AVMetadataMachineReadableCodeObject
            if (readableObject != null && readableObject.type == AVMetadataObjectTypeQRCode) {
                readableObject.stringValue?.let { qrValue ->
                    onQrCodeDetected(qrValue)
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
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
    val delegate = remember { QRMetadataDelegate(onQrCodeDetected) }
    val captureSession = remember { AVCaptureSession() }
    
    val device = remember(lensFacing) {
        val position = if (lensFacing == LensFacing.FRONT) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack
        AVCaptureDevice.defaultDeviceWithDeviceType(
            deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
            mediaType = AVMediaTypeVideo,
            position = position
        )
    }
    
    // Toggle flashlight/torch
    LaunchedEffect(flashlightEnabled, device) {
        if (device != null && device.hasTorch) {
            try {
                if (device.lockForConfiguration(null)) {
                    device.torchMode = if (flashlightEnabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
                    device.unlockForConfiguration()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Apply session preset based on resolution
    LaunchedEffect(targetResolution, captureSession) {
        if (targetResolution != null) {
            val preset = when {
                targetResolution.width >= 1920 || targetResolution.height >= 1080 -> AVCaptureSessionPreset1920x1080
                targetResolution.width >= 1280 || targetResolution.height >= 720 -> AVCaptureSessionPreset1280x720
                else -> AVCaptureSessionPreset640x480
            }
            if (captureSession.canSetSessionPreset(preset)) {
                captureSession.sessionPreset = preset
            }
        }
    }

    DisposableEffect(lensFacing) {
        if (device != null) {
            try {
                val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as? AVCaptureDeviceInput
                if (input != null && captureSession.canAddInput(input)) {
                    captureSession.addInput(input)
                }
                
                val output = AVCaptureMetadataOutput()
                if (captureSession.canAddOutput(output)) {
                    captureSession.addOutput(output)
                    output.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
                    output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                }
                
                captureSession.startRunning()
            } catch (e: Exception) {
                onCameraError(e)
            }
        }
        
        onDispose {
            captureSession.stopRunning()
        }
    }

    UIKitView(
        factory = {
            val view = UIView()
            view.backgroundColor = UIColor.blackColor
            
            if (device != null) {
                val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(captureSession)
                previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                view.layer.addSublayer(previewLayer)
            }
            view
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            val previewLayer = view.layer.sublayers?.firstOrNull() as? AVCaptureVideoPreviewLayer
            if (previewLayer != null) {
                previewLayer.frame = view.bounds
            }
        }
    )
}
