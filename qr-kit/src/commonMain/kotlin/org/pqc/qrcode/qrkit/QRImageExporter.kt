package org.pqc.qrcode.qrkit

public expect fun exportQRCodeImage(
    config: QRCodeConfig,
    fileName: String = "qrcode.png"
)
