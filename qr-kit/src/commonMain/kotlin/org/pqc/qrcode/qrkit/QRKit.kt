package org.pqc.qrcode.qrkit

import qrcode.QRCode

/**
 * QRKit is the core developer API exposing programmatic access to QR code generation
 * without requiring Compose UI. Useful for backend, database caching, or custom implementations.
 */
public object QRKit {

    /**
     * Generates a 2D boolean matrix representing the QR code, where true represents a dark module.
     */
    public fun generateMatrix(content: String): Array<Array<Boolean>> {
        val rawData = QRCode(content).rawData
        return Array(rawData.size) { r ->
            Array(rawData[r].size) { c ->
                rawData[r][c]?.dark ?: false
            }
        }
    }

    /**
     * Generates the raw PNG image bytes for a standard black-and-white QR code.
     */
    public fun generatePngBytes(content: String): ByteArray {
        return QRCode(content).render().getBytes()
    }
}
