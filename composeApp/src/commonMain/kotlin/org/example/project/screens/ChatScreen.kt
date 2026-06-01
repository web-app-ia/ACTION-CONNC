package org.example.project.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.components.DocumentViewerPanel
import org.example.project.components.QuizView
import org.example.project.components.FlashcardView
import org.example.project.domain.ChatMessage
import org.example.project.domain.MessageRole
import org.example.project.domain.Quiz
import org.example.project.domain.Flashcard
import org.example.project.presentation.SocraticViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SocraticViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TUTORIA'IAD",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                    val metadata = message.metadata
                    if (metadata?.mcpWidget != null) {
                        McpWidgetRenderer(metadata.mcpWidget.data)
                    }
                }

                if (uiState.isProcessing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                if (uiState.referencedChunks.isNotEmpty()) {
                    item {
                        DocumentViewerPanel(chunks = uiState.referencedChunks)
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Pose ta question...") },
                    shape = RoundedCornerShape(24.dp),
                    enabled = !uiState.isProcessing
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !uiState.isProcessing
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Envoyer")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun McpWidgetRenderer(widgetData: String) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    try {
        val obj = json.parseToJsonElement(widgetData).jsonObject
        val widgetType = obj["widget_type"]?.jsonPrimitive?.content ?: return
        val data = obj["data"]?.jsonObject ?: return

        when (widgetType) {
            "quiz_option" -> {
                val quiz = Quiz(
                    id = data["id"]?.jsonPrimitive?.content ?: "",
                    question = data["question"]?.jsonPrimitive?.content ?: "",
                    options = data["options"]?.let {
                        json.parseToJsonElement(it.jsonPrimitive.content).jsonObject.values.map { v ->
                            v.jsonPrimitive.content
                        }
                    } ?: emptyList(),
                    correctAnswerIndex = data["correctAnswerIndex"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    explanation = data["explanation"]?.jsonPrimitive?.content
                )
                QuizView(quiz = {}, onAnswer = {})
            }
            "flashcard" -> {
                val flashcard = Flashcard(
                    id = data["id"]?.jsonPrimitive?.content ?: "",
                    front = data["front"]?.jsonPrimitive?.content ?: "",
                    back = data["back"]?.jsonPrimitive?.content ?: ""
                )
                FlashcardView(flashcard = flashcard)
            }
        }
    } catch (_: Exception) { }
}
