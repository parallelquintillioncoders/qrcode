package org.pqc.qrcode.qrkit

import kotlin.test.Test
import kotlin.test.assertTrue
import qrcode.QRCode

class QRTest {
    @Test
    fun testQRMatrixGeneration() {
        val qr = QRCode("Hello World")
        val rawData = qr.rawData
        assertTrue(rawData.isNotEmpty(), "QR matrix raw data should not be empty")
        assertTrue(rawData[0].isNotEmpty(), "QR matrix columns should not be empty")
        
        // Assert finder pattern at top-left is dark
        assertTrue(rawData[0][0].dark, "Top-left module should be dark")
    }
}
