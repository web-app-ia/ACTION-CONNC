package org.example.project.core

actual fun currentTimeMillis(): Long = js("Date.now()").unsafeCast<Long>()
