package org.pqc.qrcode.qrkit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import qrcode.QRCode
import qrcode.raw.ErrorCorrectionLevel

public enum class QRCodeShape {
    Squares,
    Circles,
    RoundedSquares
}

@Composable
public fun QRCodeView(
    config: QRCodeConfig,
    modifier: Modifier = Modifier,
    logo: Painter? = null,
    logoScale: Float = 0.22f
) {
    QRCodeView(
        content = config.content,
        modifier = modifier,
        primaryColor = config.primaryColor,
        primaryBrush = config.primaryBrush,
        backgroundColor = config.backgroundColor,
        shape = config.shape,
        paddingPx = config.paddingPx,
        logo = logo,
        logoScale = logoScale
    )
}

@Composable
public fun QRCodeView(
    content: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color.Black,
    primaryBrush: Brush? = null,
    backgroundColor: Color = Color.White,
    shape: QRCodeShape = QRCodeShape.Squares,
    paddingPx: Int = 16,
    logo: Painter? = null,
    logoScale: Float = 0.22f
) {
    // Generate QR matrix
    val qr = remember(content, logo) {
        val ecl = if (logo != null) ErrorCorrectionLevel.HIGH else ErrorCorrectionLevel.MEDIUM
        QRCode.ofSquares()
            .withErrorCorrectionLevel(ecl)
            .build(content)
    }
    
    val rawData = qr.rawData
    val matrixSize = rawData.size
    
    val brush = primaryBrush ?: remember(primaryColor) { SolidColor(primaryColor) }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background
            drawRect(color = backgroundColor)
            
            // Apply padding to available drawing area
            val actualPadding = paddingPx.toFloat()
            val availableSize = size.minDimension - (actualPadding * 2)
            val cellSize = availableSize / matrixSize
            val xOffset = (size.width - availableSize) / 2 + actualPadding
            val yOffset = (size.height - availableSize) / 2 + actualPadding
            
            // Calculate center region to skip if logo is present
            val center = matrixSize / 2f
            val skipRadius = if (logo != null) (matrixSize * logoScale / 2f) else 0f
            
            for (row in 0 until matrixSize) {
                for (col in 0 until matrixSize) {
                    val square = rawData[row][col]
                    if (square.dark) {
                        // Check if this module is in the center logo area
                        if (logo != null && 
                            row >= (center - skipRadius) && row <= (center + skipRadius) &&
                            col >= (center - skipRadius) && col <= (center + skipRadius)) {
                            continue
                        }
                        
                        val x = xOffset + col * cellSize
                        val y = yOffset + row * cellSize
                        
                        when (shape) {
                            QRCodeShape.Squares -> {
                                drawRect(
                                    brush = brush,
                                    topLeft = Offset(x, y),
                                    size = Size(cellSize + 0.5f, cellSize + 0.5f)
                                )
                            }
                            QRCodeShape.Circles -> {
                                drawCircle(
                                    brush = brush,
                                    center = Offset(x + cellSize / 2, y + cellSize / 2),
                                    radius = (cellSize / 2) * 0.9f
                                )
                            }
                            QRCodeShape.RoundedSquares -> {
                                val rectSize = cellSize * 0.95f
                                val pad = (cellSize - rectSize) / 2
                                val rx = rectSize * 0.25f
                                val ry = rectSize * 0.25f
                                val path = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            left = x + pad,
                                            top = y + pad,
                                            right = x + pad + rectSize,
                                            bottom = y + pad + rectSize,
                                            radiusX = rx,
                                            radiusY = ry
                                        )
                                    )
                                }
                                drawPath(path = path, brush = brush)
                            }
                        }
                    }
                }
            }
        }
        
        // Draw Logo Overlay
        if (logo != null) {
            Box(
                modifier = Modifier.fillMaxSize(logoScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    with(logo) {
                        draw(size = size)
                    }
                }
            }
        }
    }
}
