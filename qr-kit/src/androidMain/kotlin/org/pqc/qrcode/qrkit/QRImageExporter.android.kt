package org.pqc.qrcode.qrkit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Environment
import qrcode.QRCode
import java.io.File
import java.io.FileOutputStream

public actual fun exportQRCodeImage(
    config: QRCodeConfig,
    fileName: String
) {
    try {
        val matrix = QRCode(config.content).rawData
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Draw White Background
        paint.color = android.graphics.Color.WHITE
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        
        val cols = matrix.size
        val cellSize = size.toFloat() / cols
        
        val startColor = parseAndroidHexColor(config.startColorHex, android.graphics.Color.BLACK)
        val endColor = parseAndroidHexColor(config.endColorHex, android.graphics.Color.BLACK)
        
        if (config.useGradient) {
            paint.shader = LinearGradient(0f, 0f, size.toFloat(), size.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)
        } else {
            paint.color = startColor
        }
        
        for (row in 0 until cols) {
            for (col in 0 until cols) {
                val cell = matrix[row][col]
                if (cell != null && cell.dark) {
                    val x = col * cellSize
                    val y = row * cellSize
                    
                    when (config.shape) {
                        QRCodeShape.Squares -> {
                            canvas.drawRect(x, y, x + cellSize + 1f, y + cellSize + 1f, paint)
                        }
                        QRCodeShape.Circles -> {
                            val pad = cellSize * 0.05f
                            val diameter = cellSize * 0.9f
                            canvas.drawCircle(x + cellSize / 2, y + cellSize / 2, diameter / 2, paint)
                        }
                        QRCodeShape.RoundedSquares -> {
                            val rectSize = cellSize * 0.9f
                            val pad = (cellSize - rectSize) / 2
                            val rx = rectSize * 0.25f
                            val rect = RectF(x + pad, y + pad, x + pad + rectSize, y + pad + rectSize)
                            canvas.drawRoundRect(rect, rx, rx, paint)
                        }
                    }
                }
            }
        }
        
        // Draw logo placeholder
        if (config.embedLogo) {
            paint.shader = null
            val logoSize = (size * 0.2f).toInt()
            val logoX = (size - logoSize) / 2f
            val logoY = (size - logoSize) / 2f
            
            // White background card
            paint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(
                RectF(logoX - 4, logoY - 4, logoX + logoSize + 4, logoY + logoSize + 4),
                12f, 12f, paint
            )
            // Blue inner circle Compose placeholder
            paint.color = android.graphics.Color.parseColor("#64B5F6")
            canvas.drawCircle(logoX + logoSize / 2f, logoY + logoSize / 2f, logoSize / 2f, paint)
        }
        
        // Write to Downloads directory
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (dir != null) {
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun parseAndroidHexColor(hex: String, fallback: Int): Int {
    val clean = hex.trim().removePrefix("#")
    return try {
        if (clean.length == 6) {
            android.graphics.Color.parseColor("#$clean")
        } else if (clean.length == 8) {
            android.graphics.Color.parseColor("#$clean")
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}
