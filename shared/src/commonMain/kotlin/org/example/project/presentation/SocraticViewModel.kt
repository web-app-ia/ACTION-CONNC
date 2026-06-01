package org.example.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.SpeechManager
import org.example.project.core.currentTimeMillis
import org.example.project.data.cache.ChatMessageEntity
import org.example.project.data.cache.DocumentEntity
import org.example.project.data.cache.LocalDatabase
import org.example.project.data.rag.RagEngine
import org.example.project.data.rag.model.Chunk
import org.example.project.data.rag.parser.FileParserRegistry
import org.example.project.data.remote.IAiService
import org.example.project.domain.ChatMessage
import org.example.project.domain.ChatMetadata
import org.example.project.domain.McpWidgetData
import org.example.project.domain.MessageRole
import kotlinx.serialization.json.Json

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val error: String? = null,
    val referencedChunks: List<Chunk> = emptyList(),
    val currentWidget: McpWidgetData? = null,
    val isRecording: Boolean = false
)

class SocraticViewModel(
    private val aiService: IAiService,
    private val database: LocalDatabase,
    private val ragEngine: RagEngine,
    private val speechManager: SpeechManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private var sessionId: String = ""

    fun startSession(studentId: String) {
        viewModelScope.launch {
            sessionId = "session_${currentTimeMillis()}"
            val session = org.example.project.data.cache.StudySessionEntity(
                id = sessionId,
                studentId = studentId,
                startTime = currentTimeMillis()
            )
            database.insertSession(session)
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            if (text.isBlank()) return@launch

            val userMessage = ChatMessage(
                id = "msg_${currentTimeMillis()}",
                role = MessageRole.USER,
                content = text,
                timestamp = currentTimeMillis()
            )

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                isProcessing = true,
                error = null
            )

            saveMessage(userMessage)

            val ragContext = ragEngine.search(text)
            val contextText = if (ragContext.isNotEmpty()) {
                ragContext.joinToString("\n\n") { it.content }
            } else ""

            val fullPrompt = buildPrompt(text, contextText)

            try {
                val response = aiService.generateContent(fullPrompt)
                val (cleanResponse, widget) = parseMcpResponse(response)

                val referencedChunks = if (ragContext.isNotEmpty()) {
                    ragContext.map { it.metadata }.distinct().let { metas ->
                        ragContext.filter { c -> c.metadata in metas }
                    }
                } else emptyList()

                val assistantMessage = ChatMessage(
                    id = "msg_${currentTimeMillis()}",
                    role = MessageRole.ASSISTANT,
                    content = cleanResponse,
                    timestamp = currentTimeMillis(),
                    metadata = ChatMetadata(
                        sourceFile = referencedChunks.firstOrNull()?.metadata?.sourceFile,
                        fileType = referencedChunks.firstOrNull()?.metadata?.fileType,
                        pageNumber = referencedChunks.firstOrNull()?.metadata?.pageNumber,
                        localUri = referencedChunks.firstOrNull()?.metadata?.localUri,
                        mcpWidget = widget
                    )
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage,
                    isProcessing = false,
                    referencedChunks = referencedChunks,
                    currentWidget = widget
                )

                saveMessage(assistantMessage)

                speechManager?.speak(cleanResponse)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Erreur: ${e.message}"
                )
            }
        }
    }

    fun toggleRecording() {
        val manager = speechManager ?: return
        if (_uiState.value.isRecording) {
            manager.stopListening()
            _uiState.value = _uiState.value.copy(isRecording = false)
        } else {
            manager.startListening { text ->
                if (text.isNotBlank()) {
                    sendMessage(text)
                }
            }
            _uiState.value = _uiState.value.copy(isRecording = true)
        }
    }

    fun uploadFile(content: ByteArray, fileName: String) {
        viewModelScope.launch {
            val fileType = fileName.substringAfterLast('.', "txt")
            val chunks = FileParserRegistry.parseAndChunk(content, fileName, fileType)

            if (chunks.isNotEmpty()) {
                chunks.forEach { ragEngine.indexDocument(
                    content = it.content,
                    sourceFile = it.metadata.sourceFile,
                    fileType = it.metadata.fileType,
                    pageNumber = it.metadata.pageNumber,
                    localUri = it.metadata.localUri
                ) }

                database.insertDocument(
                    DocumentEntity(
                        id = "doc_${currentTimeMillis()}",
                        fileName = fileName,
                        fileType = fileType,
                        localUri = fileName,
                        indexedAt = currentTimeMillis()
                    )
                )

                val msg = ChatMessage(
                    id = "msg_${currentTimeMillis()}",
                    role = MessageRole.USER,
                    content = "[Fichier importé: $fileName - ${chunks.size} extraits indexés]",
                    timestamp = currentTimeMillis()
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + msg
                )
                saveMessage(msg)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Impossible de parser le fichier: $fileName"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun saveMessage(message: ChatMessage) {
        viewModelScope.launch {
            database.insertMessage(
                ChatMessageEntity(
                    id = message.id,
                    sessionId = sessionId,
                    role = message.role.name,
                    content = message.content,
                    timestamp = message.timestamp,
                    sourceFile = message.metadata?.sourceFile,
                    fileType = message.metadata?.fileType,
                    pageNumber = message.metadata?.pageNumber,
                    localUri = message.metadata?.localUri,
                    mcpWidgetData = message.metadata?.mcpWidget?.let { json.encodeToString(McpWidgetData.serializer(), it) }
                )
            )
        }
    }

    private fun buildPrompt(userMessage: String, ragContext: String): String {
        val contextPart = if (ragContext.isNotBlank()) {
            "\n\nContexte RAG:\n$ragContext"
        } else ""
        return "$userMessage$contextPart"
    }

    private fun parseMcpResponse(response: String): Pair<String, McpWidgetData?> {
        val mcpRegex = """\{["']mcp_action["']\s*:\s*["']display_widget["'].*?\}""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = mcpRegex.find(response)

        if (match != null) {
            return try {
                val widgetData = json.decodeFromString<McpWidgetData>(match.value)
                val cleanText = response.replace(match.value, "").trim()
                Pair(cleanText, widgetData)
            } catch (e: Exception) {
                Pair(response, null)
            }
        }
        return Pair(response, null)
    }
}
