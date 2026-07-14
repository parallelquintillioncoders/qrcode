package org.pqc.qrcode.qrkit

import qrcode.QRCode

public actual fun exportQRCodeImage(
    config: QRCodeConfig,
    fileName: String
) {
    try {
        val standardData = QRCode(config.content).render().getBytes()
        val base64 = encodeBase64(standardData)
        triggerBrowserDownload(base64, fileName)
    } catch (e: Exception) {
        // Ignore
    }
}

private fun triggerBrowserDownload(base64Data: String, fileName: String) {
    js("""
        var a = document.createElement('a');
        a.href = 'data:image/png;base64,' + base64Data;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    """)
}

private fun encodeBase64(bytes: ByteArray): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val builder = StringBuilder()
    var i = 0
    val size = bytes.size
    while (i < size) {
        val b0 = bytes[i].toInt() and 0xFF
        val b1 = if (i + 1 < size) bytes[i + 1].toInt() and 0xFF else -1
        val b2 = if (i + 2 < size) bytes[i + 2].toInt() and 0xFF else -1
        
        val c0 = b0 shr 2
        val c1 = ((b0 and 3) shl 4) or (if (b1 != -1) b1 shr 4 else 0)
        val c2 = if (b1 != -1) ((b1 and 15) shl 2) or (if (b2 != -1) b2 shr 6 else 0) else -1
        val c3 = if (b2 != -1) b2 and 63 else -1
        
        builder.append(chars[c0])
        builder.append(chars[c1])
        builder.append(if (c2 != -1) chars[c2] else '=')
        builder.append(if (c3 != -1) chars[c3] else '=')
        i += 3
    }
    return builder.toString()
}
