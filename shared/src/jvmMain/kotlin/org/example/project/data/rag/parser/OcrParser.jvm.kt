package org.example.project.data.rag.parser

import net.sourceforge.tess4j.Tesseract
import java.io.File
import javax.imageio.ImageIO

actual suspend fun ocrImage(bytes: ByteArray, fileName: String): String {
    return try {
        val tempFile = File.createTempFile("ocr_", "_$fileName")
        tempFile.writeBytes(bytes)
        val tesseract = Tesseract()
        tesseract.setLanguage("fra")
        tesseract.setDatapath(System.getenv("TESSDATA_PREFIX") ?: "/usr/share/tesseract-ocr/4.00/tessdata")
        val result = tesseract.doOCR(tempFile)
        tempFile.delete()
        result
    } catch (e: Exception) {
        "[OCR JVM] Erreur: ${e.message}"
    }
}
