package org.example.project.data.report

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream

actual suspend fun generatePdf(content: String, fileName: String): ByteArray? {
    return try {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 12f; isAntiAlias = true }
        var y = 50f
        for (line in content.lines()) {
            canvas.drawText(line, 50f, y, paint)
            y += 16f
        }
        doc.finishPage(page)
        val baos = ByteArrayOutputStream()
        doc.writeTo(baos)
        doc.close()
        baos.toByteArray()
    } catch (_: Exception) { null }
}
