package org.example.project

actual fun pickFile(): PickedFile? {
    return null // Wasm File picker requires DOM interaction; set from outside
}
