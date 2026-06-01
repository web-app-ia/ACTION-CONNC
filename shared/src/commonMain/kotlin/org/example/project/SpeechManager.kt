package org.example.project

interface SpeechManager {
    fun startListening(onResult: (String) -> Unit)
    fun stopListening()
    fun speak(text: String)
    fun stopSpeaking()
    val isListening: Boolean
    val isSpeaking: Boolean
}

expect fun createSpeechManager(): SpeechManager
