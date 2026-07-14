package org.pqc.qrcode.qrkit

import qrcode.QRCode
import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

public actual fun exportQRCodeImage(
    config: QRCodeConfig,
    fileName: String
) {
    try {
        val standardData = QRCode(config.content).render().getBytes()
        val array = Uint8Array(standardData.size)
        for (i in standardData.indices) {
            array.asDynamic()[i] = standardData[i]
        }
        val blob = Blob(arrayOf(array), BlobPropertyBag("image/png"))
        val url = URL.createObjectURL(blob)
        val a = document.createElement("a") as HTMLAnchorElement
        a.href = url
        a.download = fileName
        document.body?.appendChild(a)
        a.click()
        document.body?.removeChild(a)
        URL.revokeObjectURL(url)
    } catch (e: Exception) {
        println("Web Export failed: ${e.message}")
    }
}
