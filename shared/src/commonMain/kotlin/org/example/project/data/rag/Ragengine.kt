package org.example.project.data.rag

import org.example.project.data.rag.model.Chunk
import org.example.project.data.rag.model.ChunkMetadata

class RagEngine {

    private val chunks = mutableListOf<Chunk>()

    fun indexDocument(
        content: String,
        sourceFile: String,
        fileType: String,
        pageNumber: Int? = null,
        localUri: String? = null,
        chunkSize: Int = 500,
        overlap: Int = 50
    ): List<Chunk> {
        val metadata = ChunkMetadata(
            sourceFile = sourceFile,
            fileType = fileType,
            pageNumber = pageNumber,
            localUri = localUri
        )
        val newChunks = chunkText(content, sourceFile, metadata, chunkSize, overlap)
        chunks.addAll(newChunks)
        return newChunks
    }

    fun search(query: String, maxResults: Int = 5): List<Chunk> {
        if (chunks.isEmpty()) return emptyList()
        val queryLower = query.lowercase()
        val queryTerms = queryLower.split("\\s+".toRegex()).filter { it.length > 2 }

        val scored = chunks.map { chunk ->
            val contentLower = chunk.content.lowercase()
            val score = queryTerms.sumOf { term ->
                if (contentLower.contains(term)) 1 else 0
            }
            chunk to score
        }

        return scored
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    fun getAllChunks(): List<Chunk> = chunks.toList()

    fun clear() {
        chunks.clear()
    }

    private fun chunkText(
        text: String,
        sourceFile: String,
        metadata: ChunkMetadata,
        chunkSize: Int,
        overlap: Int
    ): List<Chunk> {
        if (text.isBlank()) return emptyList()
        val result = mutableListOf<Chunk>()
        val paragraphs = text.split("\n\n").filter { it.isNotBlank() }
        var currentChunk = StringBuilder()
        var chunkIndex = 0

        for (paragraph in paragraphs) {
            if (currentChunk.length + paragraph.length > chunkSize && currentChunk.isNotEmpty()) {
                result.add(
                    Chunk(
                        id = "${sourceFile}_chunk_$chunkIndex",
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
                    id = "${sourceFile}_chunk_$chunkIndex",
                    content = currentChunk.toString().trim(),
                    metadata = metadata
                )
            )
        }

        return result
    }
}
