package org.pqc.qrcode.qrkit

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * QRCodeConfig encapsulates all custom styling, payload, and layout variables
 * for a QR code. Pass this single configuration object to QRCodeView and exportQRCodeImage.
 */
public data class QRCodeConfig(
    val content: String,
    val shape: QRCodeShape = QRCodeShape.Squares,
    val primaryColor: Color = Color.Black,
    val primaryBrush: Brush? = null,
    val backgroundColor: Color = Color.White,
    val startColorHex: String = "#000000",
    val endColorHex: String = "#000000",
    val useGradient: Boolean = false,
    val embedLogo: Boolean = false,
    val sizeDp: Int = 240,
    val paddingPx: Int = 16
)
