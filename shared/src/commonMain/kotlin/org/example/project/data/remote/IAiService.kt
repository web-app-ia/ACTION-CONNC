package org.example.project.data.remote

interface IAiService {
    suspend fun generateContent(prompt: String): String
}
