package org.example.project.domain

import kotlinx.serialization.Serializable

@Serializable
data class ChunkMetadata(
    val sourceFile: String,
    val fileType: String,
    val pageNumber: Int? = null,
    val localUri: String? = null
)
