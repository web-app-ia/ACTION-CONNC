package org.example.project.domain

data class ServerConfig(
    val serverUrl: String = "http://localhost:11434",
    val apiEndpoint: String = "/v1/chat/completions",
    val modelName: String = "llama3",
    val apiKey: String = ""
)
