package org.example.project.data.rag.model

data class ChunkMetadata(
    val source: String,
    val pageNumber: Int? = null,
    val position: Int? = null
)
