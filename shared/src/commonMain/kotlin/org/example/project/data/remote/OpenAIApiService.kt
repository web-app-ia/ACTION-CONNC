package org.example.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.core.Constants

class OpenAIApiService(private val httpClient: HttpClient) : IAiService {

    // Configure to use a locally running OpenAI-compatible server
    private val LOCAL_LLM_SERVER_URL = "http://localhost:11434" // Default Ollama endpoint
    private val OPENAI_COMPATIBLE_ENDPOINT = "/v1/chat/completions" // Common endpoint for OpenAI-compatible servers

    override suspend fun generateContent(prompt: String): String {
        try {
            val requestBody = OpenAIRequest(
                model = "local-model", // This will be determined by the local server's configuration
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = Constants.PROMPT_SYSTEME
                    ),
                    ChatMessage(
                        role = "user",
                        content = prompt
                    )
                )
            )

            val response = httpClient.post {
                url("$LOCAL_LLM_SERVER_URL$OPENAI_COMPATIBLE_ENDPOINT")
                setBody(requestBody)
                // Local servers might not require an API key, or might use a different header.
                // If your local server requires a key, uncomment and set it here.
                // headers {
                //     append("Authorization", "Bearer YOUR_LOCAL_SERVER_API_KEY")
                //     append("Content-Type", "application/json")
                // }
            }

            val responseBody = response.bodyAsText()
            val openAIResponse = Json.decodeFromString<OpenAIResponse>(responseBody)

            return openAIResponse.choices.firstOrNull()?.message?.content?.trim()
                ?: "No content generated."

        } catch (e: Exception) {
            println("Error generating content from local LLM server: ${e.message}")
            // Provide a more user-friendly error message or guidance
            return "Error: Could not connect to the local AI server. Please ensure it is running and accessible at $LOCAL_LLM_SERVER_URL. Details: ${e.message}"
        }
    }

    // --- Data Classes for OpenAI API Request and Response ---

    @Serializable
    data class OpenAIRequest(
        val model: String,
        val messages: List<ChatMessage>
    )

    @Serializable
    data class ChatMessage(
        val role: String,
        val content: String
    )

    @Serializable
    data class OpenAIResponse(
        val id: String,
        val choices: List<Choice>,
        val usage: Usage
    )

    @Serializable
    data class Choice(
        val message: ChatMessage,
        val finish_reason: String
    )

    @Serializable
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}
