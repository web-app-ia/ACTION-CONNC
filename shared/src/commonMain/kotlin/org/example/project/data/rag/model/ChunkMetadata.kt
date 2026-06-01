package org.example.project.data.rag.model

import kotlinx.serialization.Serializable

@Serializable
data class ChunkMetadata(
    val sourceFile: String,
    val fileType: String,
    val pageNumber: Int? = null,
    val localUri: String? = null
)
