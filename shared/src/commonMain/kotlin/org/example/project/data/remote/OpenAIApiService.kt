package org.example.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.core.Constants
import org.example.project.domain.ServerConfig

class OpenAIApiService(
    private val httpClient: HttpClient,
    private var config: ServerConfig = ServerConfig()
) : IAiService {

    fun updateConfig(newConfig: ServerConfig) {
        config = newConfig
    }

    fun getConfig(): ServerConfig = config

    override suspend fun generateContent(prompt: String): String {
        try {
            val requestBody = OpenAIRequest(
                model = config.modelName,
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
                url("${config.serverUrl}${config.apiEndpoint}")
                contentType(ContentType.Application.Json)
                if (config.apiKey.isNotBlank()) {
                    header("Authorization", "Bearer ${config.apiKey}")
                }
                setBody(requestBody)
            }

            val responseBody = response.bodyAsText()
            val openAIResponse = Json.decodeFromString<OpenAIResponse>(responseBody)

            return openAIResponse.choices.firstOrNull()?.message?.content?.trim()
                ?: "No content generated."

        } catch (e: Exception) {
            println("Error generating content from local LLM server: ${e.message}")
            return "Error: Could not connect to the AI server at ${config.serverUrl}. Details: ${e.message}"
        }
    }

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
