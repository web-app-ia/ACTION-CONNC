package org.example.project

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
    val sizeBytes: Long
) {
    val fileType: String get() = name.substringAfterLast('.', "txt").lowercase()
    val isImage: Boolean get() = fileType in listOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
    val isPdf: Boolean get() = fileType == "pdf"
    val isText: Boolean get() = fileType in listOf("txt", "md", "csv", "json", "xml", "html", "htm")
    val isAudio: Boolean get() = fileType in listOf("mp3", "wav", "ogg", "m4a", "flac")
}

expect fun pickFile(): PickedFile?
