package org.example.project.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.domain.ChatMessage
import org.example.project.domain.Flashcard
import org.example.project.domain.MessageRole
import org.example.project.domain.Quiz
import org.example.project.presentation.SocraticViewModel
import org.example.project.ui.components.DocumentViewerPanel
import org.example.project.ui.components.FlashcardView
import org.example.project.ui.components.QuizView
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SocraticViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TUTORIA'IAD", fontWeight = FontWeight.Bold)
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

            val errorText = uiState.error
            if (errorText != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Erreur") },
                    text = { Text(errorText) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                    }
                )
            }

            HorizontalDivider()

            if (uiState.isRecording) {
                RecordingBar(onStop = { viewModel.toggleRecording() })
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val file = org.example.project.pickFile()
                                if (file != null) {
                                    viewModel.uploadFile(file.bytes, file.name)
                                }
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isProcessing
                    ) {
                        Text("📎", fontSize = 16.sp)
                    }

                    Spacer(Modifier.width(4.dp))

                    OutlinedButton(
                        onClick = { viewModel.toggleRecording() },
                        modifier = Modifier.height(40.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        enabled = !uiState.isProcessing
                    ) {
                        Text("🎤", fontSize = 16.sp)
                    }

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Pose ta question...") },
                        shape = RoundedCornerShape(24.dp),
                        enabled = !uiState.isProcessing,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isProcessing,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("➤", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun RecordingBar(onStop: () -> Unit) {
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    val spectrogramHeights = remember { mutableStateListOf<Float>() }
    val infiniteTransition = rememberInfiniteTransition(label = "spectro")

    if (spectrogramHeights.isEmpty()) {
        repeat(24) { spectrogramHeights.add(Random.nextFloat()) }
    }

    val barCount = 24
    val animatedHeights = remember { mutableStateListOf<Float>() }
    if (animatedHeights.isEmpty()) {
        repeat(barCount) { animatedHeights.add(0.2f) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(80)
            for (i in 0 until barCount) {
                val time = System.currentTimeMillis() / 1000.0
                val wave = (sin(time * 8.0 + i * 0.5) + 1.0) / 2.0
                val noise = Random.nextFloat() * 0.3f
                animatedHeights[i] = (wave.toFloat() * 0.7f + noise).coerceIn(0.1f, 1f)
            }
        }
    }

    val minutes = seconds / 60
    val secs = seconds % 60
    val timerText = "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.weight(1f).height(32.dp)) {
                    val barWidth = size.width / barCount
                    for (i in 0 until barCount) {
                        val h = size.height * animatedHeights.getOrElse(i) { 0.2f }
                        val barX = i * barWidth + barWidth * 0.1f
                        val barW = barWidth * 0.8f
                        drawRoundRect(
                            color = Color(0xFFE53935),
                            topLeft = Offset(barX, size.height - h),
                            size = androidx.compose.ui.geometry.Size(barW, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2, barW / 2)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = timerText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(Modifier.width(4.dp))

                FilledTonalButton(
                    onClick = onStop,
                    modifier = Modifier.height(32.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("⏹", fontSize = 14.sp)
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

private data class ParsedWidget(
    val widgetType: String,
    val quiz: Quiz? = null,
    val flashcard: Flashcard? = null
)

@Composable
private fun McpWidgetRenderer(widgetData: String) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val parsed = remember(widgetData) {
        try {
            val obj = json.parseToJsonElement(widgetData).jsonObject
            val widgetType = obj["widget_type"]?.jsonPrimitive?.content ?: return@remember null
            val data = obj["data"]?.jsonObject

            when (widgetType) {
                "quiz_option" -> {
                    val quiz = Quiz(
                        id = data?.get("id")?.jsonPrimitive?.content ?: "",
                        question = data?.get("question")?.jsonPrimitive?.content ?: "",
                        options = emptyList(),
                        correctAnswerIndex = data?.get("correctAnswerIndex")?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                        explanation = data?.get("explanation")?.jsonPrimitive?.content
                    )
                    ParsedWidget(widgetType = widgetType, quiz = quiz)
                }
                "flashcard" -> {
                    val flashcard = Flashcard(
                        id = data?.get("id")?.jsonPrimitive?.content ?: "",
                        front = data?.get("front")?.jsonPrimitive?.content ?: "",
                        back = data?.get("back")?.jsonPrimitive?.content ?: ""
                    )
                    ParsedWidget(widgetType = widgetType, flashcard = flashcard)
                }
                else -> null
            }
        } catch (_: Exception) { null }
    }

    when (parsed?.widgetType) {
        "quiz_option" -> parsed.quiz?.let { QuizView(quiz = it, onAnswer = {}) }
        "flashcard" -> parsed.flashcard?.let { FlashcardView(flashcard = it) }
    }
}
