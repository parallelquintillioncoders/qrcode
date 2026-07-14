package org.pqc.qrcode.qrkit

public expect fun exportQRCodeImage(
    content: String,
    shape: QRCodeShape,
    startColorHex: String,
    endColorHex: String,
    useGradient: Boolean,
    embedLogo: Boolean,
    fileName: String = "qrcode.png"
)
