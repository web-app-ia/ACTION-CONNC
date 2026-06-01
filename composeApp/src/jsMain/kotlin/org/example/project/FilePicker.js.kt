package org.example.project

actual fun pickFile(): PickedFile? {
    return null // JS File picker requires DOM interaction; set from outside
}
