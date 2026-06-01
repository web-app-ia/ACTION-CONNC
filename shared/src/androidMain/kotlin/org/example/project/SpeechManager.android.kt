package org.example.project

class AndroidSpeechManager : SpeechManager {
    override var isListening: Boolean = false
    override var isSpeaking: Boolean = false
    private var onResultCallback: ((String) -> Unit)? = null

    override fun startListening(onResult: (String) -> Unit) {
        this.onResultCallback = onResult
        isListening = true
    }

    override fun stopListening() {
        isListening = false
    }

    override fun speak(text: String) {
        isSpeaking = true
    }

    override fun stopSpeaking() {
        isSpeaking = false
    }
}

actual fun createSpeechManager(): SpeechManager = AndroidSpeechManager()
