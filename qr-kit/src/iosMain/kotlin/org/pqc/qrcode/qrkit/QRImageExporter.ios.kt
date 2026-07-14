package org.pqc.qrcode.qrkit

import qrcode.QRCode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
public actual fun exportQRCodeImage(
    config: QRCodeConfig,
    fileName: String
) {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDirectory = paths.firstOrNull() as? String
    if (documentsDirectory != null) {
        val filePath = "$documentsDirectory/$fileName"
        val standardData = QRCode(config.content).render().getBytes()
        
        val nsData = standardData.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = standardData.size.toULong())
        }
        nsData.writeToFile(filePath, atomically = true)
    }
}
