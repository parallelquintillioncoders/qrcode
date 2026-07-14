package org.pqc.qrcode

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform