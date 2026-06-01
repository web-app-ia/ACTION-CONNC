package org.example.project.data.report

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.PDPageContentStream
import java.io.ByteArrayOutputStream

actual suspend fun generatePdf(content: String, fileName: String): ByteArray? {
    return try {
        val doc = PDDocument()
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        val font = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        val stream = PDPageContentStream(doc, page)
        stream.setLeading(14f)
        stream.beginText()
        stream.newLineAtOffset(50f, 750f)
        stream.setFont(font, 11f)
        content.lines().forEach { line ->
            stream.showText(line)
            stream.newLine()
        }
        stream.endText()
        stream.close()
        val baos = ByteArrayOutputStream()
        doc.save(baos)
        doc.close()
        baos.toByteArray()
    } catch (_: Exception) { null }
}
