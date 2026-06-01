package org.example.project.domain

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val metadata: ChatMetadata? = null
)

enum class MessageRole {
    SYSTEM, USER, ASSISTANT
}

data class ChatMetadata(
    val sourceFile: String? = null,
    val fileType: String? = null,
    val pageNumber: Int? = null,
    val localUri: String? = null,
    val mcpWidget: McpWidgetData? = null
)

@kotlinx.serialization.Serializable
data class McpWidgetData(
    val widgetType: String,
    val data: String
)
