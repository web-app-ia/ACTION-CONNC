package org.example.project.data.rag.model

import kotlinx.serialization.Serializable

@Serializable
data class Chunk(
    val id: String,
    val content: String,
    val metadata: ChunkMetadata,
    val embedding: List<Float>? = null
)
