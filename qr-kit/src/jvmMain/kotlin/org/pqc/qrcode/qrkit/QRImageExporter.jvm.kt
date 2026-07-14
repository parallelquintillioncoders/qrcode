package org.pqc.qrcode.qrkit

import qrcode.QRCode
import java.awt.Color as AwtColor
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

public actual fun exportQRCodeImage(
    config: QRCodeConfig,
    fileName: String
) {
    try {
        val home = System.getProperty("user.home") ?: "."
        val downloadsDir = File(home, "Downloads")
        val file = if (downloadsDir.exists()) {
            File(downloadsDir, fileName)
        } else {
            File(home, fileName)
        }
        
        val matrix = QRCode(config.content).rawData
        // Scale the user's size slightly for better export quality, or just use it. Let's use config.sizeDp * 2 for higher res, or just max(512, config.sizeDp)
        val exportScale = 2f
        val size = (config.sizeDp * exportScale).toInt()
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2 = image.createGraphics()
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = AwtColor.WHITE
        g2.fillRect(0, 0, size, size)
        
        val actualPadding = config.paddingPx * exportScale
        val availableSize = size - (actualPadding * 2)
        
        val cols = matrix.size
        val cellSize = availableSize / cols
        
        val actualQrSize = cellSize * cols
        val xOffset = (size - actualQrSize) / 2f
        val yOffset = (size - actualQrSize) / 2f
        
        val startColor = parseAwtHexColor(config.startColorHex, AwtColor.BLACK)
        val endColor = parseAwtHexColor(config.endColorHex, AwtColor.BLACK)
        
        for (row in 0 until cols) {
            for (col in 0 until cols) {
                val cell = matrix[row][col]
                if (cell != null && cell.dark) {
                    val x = xOffset + col * cellSize
                    val y = yOffset + row * cellSize
                    
                    if (config.useGradient) {
                        val gp = GradientPaint(0f, 0f, startColor, size.toFloat(), size.toFloat(), endColor)
                        g2.paint = gp
                    } else {
                        g2.color = startColor
                    }
                    
                    when (config.shape) {
                        QRCodeShape.Squares -> {
                            g2.fillRect(x.toInt(), y.toInt(), (cellSize + 1f).toInt(), (cellSize + 1f).toInt())
                        }
                        QRCodeShape.Circles -> {
                            val pad = cellSize * 0.05f
                            val diameter = cellSize * 0.9f
                            g2.fillOval((x + pad).toInt(), (y + pad).toInt(), diameter.toInt(), diameter.toInt())
                        }
                        QRCodeShape.RoundedSquares -> {
                            val rectSize = cellSize * 0.9f
                            val pad = (cellSize - rectSize) / 2
                            val rx = rectSize * 0.25f
                            val roundRect = RoundRectangle2D.Float(x + pad, y + pad, rectSize, rectSize, rx, rx)
                            g2.fill(roundRect)
                        }
                    }
                }
            }
        }
        
        if (config.embedLogo) {
            val logoSize = (actualQrSize * 0.2f).toInt()
            val logoX = (size - logoSize) / 2
            val logoY = (size - logoSize) / 2
            g2.color = AwtColor.WHITE
            g2.fillRoundRect(logoX - 4, logoY - 4, logoSize + 8, logoSize + 8, 12, 12)
            g2.color = AwtColor(0x64, 0xB5, 0xF6)
            g2.fillOval(logoX, logoY, logoSize, logoSize)
        }
        
        g2.dispose()
        ImageIO.write(image, "png", file)
        println("QR Code successfully saved to: ${file.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun parseAwtHexColor(hex: String, fallback: AwtColor): AwtColor {
    val clean = hex.trim().removePrefix("#")
    return try {
        if (clean.length == 6) {
            AwtColor(clean.toInt(16))
        } else if (clean.length == 8) {
            val alpha = clean.substring(0, 2).toInt(16)
            val rgb = clean.substring(2).toInt(16)
            AwtColor((alpha shl 24) or rgb, true)
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}
