package org.example.project.data.remote

import org.example.project.core.Constants
import org.example.project.data.rag.model.Chunk
import org.example.project.data.rag.model.ChunkMetadata
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GeminiApiService(private val httpClient: HttpClient) : IAiService {

    private val GEMINI_API_URL = "YOUR_GEMINI_API_URL" // Replace with the actual Gemini API URL
    private val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY" // Replace with your actual Gemini API Key

    override suspend fun generateContent(prompt: String): String {
        try {
            val requestBody = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = "${Constants.PROMPT_SYSTEME}\n\nUser: $prompt"
                            )
                        )
                    )
                )
            )

            val response = httpClient.post {
                url("$GEMINI_API_URL?key=$GEMINI_API_KEY")
                setBody(requestBody)
            }

            val responseBody = response.bodyAsText()
            val geminiResponse = Json.decodeFromString<GeminiResponse>(responseBody)

            return geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No content generated."

        } catch (e: Exception) {
            println("Error generating content from Gemini: ${e.message}")
            return "Error: Could not generate content from Gemini. ${e.message}"
        }
    }

    // --- Data Classes for Gemini API Request and Response ---

    @Serializable
    data class GeminiRequest(
        val contents: List<Content>
    )

    @Serializable
    data class Content(
        val parts: List<Part>
    )

    @Serializable
    data class Part(
        val text: String
    )

    @Serializable
    data class GeminiResponse(
        val candidates: List<Candidate>
    )

    @Serializable
    data class Candidate(
        val content: Content
    )

    // --- Dummy data classes for RAG simulation ---
    // These would be replaced by actual data models when integrating with a vector database
    data class RAGResult(
        val chunks: List<Chunk>
    )

    data class Chunk(
        val content: String,
        val metadata: ChunkMetadata
    )
}
