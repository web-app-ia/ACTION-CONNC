package org.example.project

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual fun pickFile(): PickedFile? {
    val dialog = FileDialog(null as Frame?, "Importer un fichier", FileDialog.LOAD)
    dialog.isVisible = true

    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    val file = File(directory, fileName)
    if (!file.exists() || !file.isFile) return null

    return PickedFile(
        name = file.name,
        bytes = file.readBytes(),
        sizeBytes = file.length()
    )
}
