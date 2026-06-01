package org.example.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.example.project.data.cache.InMemoryDatabase
import org.example.project.data.network.KtorClientFactory
import org.example.project.data.rag.RagEngine
import org.example.project.data.remote.AiServiceFactory
import org.example.project.domain.ServerConfig
import org.example.project.domain.Student
import org.example.project.domain.Skill
import org.example.project.presentation.SocraticViewModel
import org.example.project.ui.screens.ChatScreen
import org.example.project.ui.screens.MonitoringScreen
import org.example.project.ui.screens.RAGLibraryManager
import org.example.project.ui.screens.SettingsScreen

enum class Screen {
    CHAT, MONITORING, RAG_LIBRARY, SETTINGS
}

@Composable
fun App() {
    val database = remember { InMemoryDatabase() }
    val ragEngine = remember { RagEngine() }
    val httpClient = remember { KtorClientFactory.createLocalClient() }
    val aiService = remember { AiServiceFactory.getAiService(httpClient) }
    val speechManager = remember { createSpeechManager() }
    val viewModel = remember { SocraticViewModel(aiService, database, ragEngine, speechManager) }

    var currentScreen by remember { mutableStateOf(Screen.CHAT) }
    var serverConfig by remember { mutableStateOf(ServerConfig()) }
    var testResult by remember { mutableStateOf<String?>(null) }

    val student = remember {
        Student(
            id = "student_1",
            name = "Élève",
            level = "CP1",
            studyTimeMinutes = 120,
            totalQuizzesTaken = 5,
            averageScore = 72.0
        )
    }
    val skills = remember {
        listOf(
            Skill(id = "s1", name = "Mathématiques", category = "Sciences", proficiency = 65.0),
            Skill(id = "s2", name = "Français", category = "Langues", proficiency = 80.0),
            Skill(id = "s3", name = "Sciences", category = "Sciences", proficiency = 45.0)
        )
    }

    var documents by remember { mutableStateOf(emptyList<org.example.project.data.cache.DocumentEntity>()) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Text("C", fontWeight = FontWeight.Bold) },
                        label = { Text("Chat") },
                        selected = currentScreen == Screen.CHAT,
                        onClick = { currentScreen = Screen.CHAT }
                    )
                    NavigationBarItem(
                        icon = { Text("S", fontWeight = FontWeight.Bold) },
                        label = { Text("Suivi") },
                        selected = currentScreen == Screen.MONITORING,
                        onClick = { currentScreen = Screen.MONITORING }
                    )
                    NavigationBarItem(
                        icon = { Text("R", fontWeight = FontWeight.Bold) },
                        label = { Text("RAG") },
                        selected = currentScreen == Screen.RAG_LIBRARY,
                        onClick = { currentScreen = Screen.RAG_LIBRARY }
                    )
                    NavigationBarItem(
                        icon = { Text("P", fontWeight = FontWeight.Bold) },
                        label = { Text("Param.") },
                        selected = currentScreen == Screen.SETTINGS,
                        onClick = { currentScreen = Screen.SETTINGS }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentScreen) {
                    Screen.CHAT -> ChatScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    Screen.MONITORING -> MonitoringScreen(
                        student = student,
                        skills = skills,
                        modifier = Modifier.fillMaxSize()
                    )
                    Screen.RAG_LIBRARY -> RAGLibraryManager(
                        documents = documents,
                        onAddDocument = { },
                        onDeleteDocument = { id ->
                            documents = documents.filter { it.id != id }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        config = serverConfig,
                        onConfigChanged = { newConfig ->
                            serverConfig = newConfig
                            AiServiceFactory.updateConfig(httpClient, newConfig)
                            testResult = "Configuration enregistrée"
                        },
                        onTestConnection = {
                            testResult = "Test non disponible (serveur distant requis)"
                        },
                        testResult = testResult,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
