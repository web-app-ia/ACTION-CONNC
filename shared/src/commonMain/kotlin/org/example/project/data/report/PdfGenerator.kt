package org.example.project.data.report

expect suspend fun generatePdf(content: String, fileName: String): ByteArray?
