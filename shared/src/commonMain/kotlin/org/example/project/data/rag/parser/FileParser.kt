package org.example.project.data.rag.parser

import org.example.project.data.rag.model.Chunk
import org.example.project.data.rag.model.ChunkMetadata

data class ParsedDocument(
    val content: String,
    val fileName: String,
    val fileType: String,
    val pageNumber: Int? = null,
    val localUri: String? = null
)

interface FileParser {
    fun canHandle(fileType: String): Boolean
    fun parse(content: ByteArray, fileName: String, fileType: String, localUri: String? = null): ParsedDocument?
}

object FileParserRegistry {
    private val parsers = mutableListOf<FileParser>()

    init {
        parsers.add(PdfTextParser())
        parsers.add(PlainTextParser())
        parsers.add(ImageParser())
    }

    fun getParser(fileType: String): FileParser? = parsers.find { it.canHandle(fileType) }

    fun parseAndChunk(
        content: ByteArray,
        fileName: String,
        fileType: String,
        localUri: String? = null,
        chunkSize: Int = 500,
        overlap: Int = 50
    ): List<Chunk> {
        val parser = getParser(fileType) ?: return emptyList()
        val doc = parser.parse(content, fileName, fileType, localUri) ?: return emptyList()
        return chunkText(doc.content, doc, chunkSize, overlap)
    }

    private fun chunkText(
        text: String,
        doc: ParsedDocument,
        chunkSize: Int,
        overlap: Int
    ): List<Chunk> {
        if (text.isBlank()) return emptyList()
        val result = mutableListOf<Chunk>()
        val paragraphs = text.split("\n\n").filter { it.isNotBlank() }
        var currentChunk = StringBuilder()
        var chunkIndex = 0

        val metadata = ChunkMetadata(
            sourceFile = doc.fileName,
            fileType = doc.fileType,
            pageNumber = doc.pageNumber,
            localUri = doc.localUri
        )

        for (paragraph in paragraphs) {
            if (currentChunk.length + paragraph.length > chunkSize && currentChunk.isNotEmpty()) {
                result.add(
                    Chunk(
                        id = "${doc.fileName}_chunk_$chunkIndex",
                        content = currentChunk.toString().trim(),
                        metadata = metadata
                    )
                )
                chunkIndex++
                val overlapText = if (overlap > 0 && currentChunk.length >= overlap) {
                    currentChunk.substring(currentChunk.length - overlap)
                } else ""
                currentChunk = StringBuilder(overlapText)
            }
            if (currentChunk.isNotEmpty()) currentChunk.append("\n\n")
            currentChunk.append(paragraph)
        }

        if (currentChunk.isNotBlank()) {
            result.add(
                Chunk(
                    id = "${doc.fileName}_chunk_$chunkIndex",
                    content = currentChunk.toString().trim(),
                    metadata = metadata
                )
            )
        }
        return result
    }
}

class PdfTextParser : FileParser {
    private val supportedTypes = listOf("pdf", "application/pdf")

    override fun canHandle(fileType: String): Boolean =
        supportedTypes.any { fileType.lowercase().contains(it) }

    override fun parse(content: ByteArray, fileName: String, fileType: String, localUri: String?): ParsedDocument? {
        val text = try {
            String(content, Charsets.UTF_8)
                .replace(Regex("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        } catch (_: Exception) { null }

        return if (text != null && text.isNotBlank()) {
            ParsedDocument(
                content = "[Extrait PDF - $fileName]\n\n$text",
                fileName = fileName,
                fileType = "pdf",
                localUri = localUri
            )
        } else {
            ParsedDocument(
                content = "[Document PDF: $fileName - Le texte intégral sera disponible avec un analyseur PDF natif]",
                fileName = fileName,
                fileType = "pdf",
                localUri = localUri
            )
        }
    }
}

class PlainTextParser : FileParser {
    private val supportedTypes = listOf("txt", "text", "text/plain", "md", "markdown", "csv", "json", "xml", "html", "htm")

    override fun canHandle(fileType: String): Boolean =
        supportedTypes.any { fileType.lowercase().contains(it) }

    override fun parse(content: ByteArray, fileName: String, fileType: String, localUri: String?): ParsedDocument {
        val text = String(content, Charsets.UTF_8)
        return ParsedDocument(
            content = text,
            fileName = fileName,
            fileType = fileType,
            localUri = localUri
        )
    }
}

class ImageParser : FileParser {
    private val supportedTypes = listOf("png", "jpg", "jpeg", "gif", "bmp", "image/")

    override fun canHandle(fileType: String): Boolean =
        supportedTypes.any { fileType.lowercase().contains(it) }

    override fun parse(content: ByteArray, fileName: String, fileType: String, localUri: String?): ParsedDocument? {
        return ParsedDocument(
            content = "[Image: $fileName - OCR en cours...]",
            fileName = fileName,
            fileType = fileType,
            localUri = localUri
        )
    }

    suspend fun parseWithOcr(content: ByteArray, fileName: String, fileType: String, localUri: String?): ParsedDocument? {
        val text = ocrImage(content, fileName)
        return ParsedDocument(
            content = text,
            fileName = fileName,
            fileType = fileType,
            localUri = localUri
        )
    }
}
