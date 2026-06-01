package org.example.project.domain

data class Document(
    val id: String,
    val fileName: String,
    val fileType: String,
    val localUri: String,
    val pageCount: Int? = null,
    val indexedAt: Long = 0
)
