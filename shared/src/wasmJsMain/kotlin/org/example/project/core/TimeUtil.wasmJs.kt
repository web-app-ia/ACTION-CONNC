package org.example.project.core

actual fun currentTimeMillis(): Long = kotlinx.browser.window.performance.timeOrigin.toLong() +
    kotlinx.browser.window.performance.now().toLong()
