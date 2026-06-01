package org.example.project.data.update

actual fun detectNetworkType(): NetworkType = NetworkType.WIFI

actual fun platformOsName(): String {
    val os = System.getProperty("os.name") ?: "unknown"
    val arch = System.getProperty("os.arch") ?: ""
    return "$os $arch"
}
