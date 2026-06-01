package org.example.project.data.update

enum class NetworkType {
    WIFI,
    MOBILE,
    UNKNOWN
}

expect fun detectNetworkType(): NetworkType
