package org.example.project.data.rag.parser

expect suspend fun ocrImage(bytes: ByteArray, fileName: String): String
