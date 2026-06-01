package org.example.project

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.os.Bundle
import java.util.Locale

class AndroidSpeechManager(private val context: android.content.Context) : SpeechManager {
    override var isListening: Boolean = false
    override var isSpeaking: Boolean = false

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var onResultCallback: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.FRENCH
            }
        }
    }

    override fun startListening(onResult: (String) -> Unit) {
        onResultCallback = onResult
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() { isListening = true }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onResults(results: Bundle?) {
                val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                texts?.firstOrNull()?.let { onResultCallback?.invoke(it) }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parle dans le micro")
        }
        speechRecognizer?.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    override fun speak(text: String) {
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
        isSpeaking = false
    }

    override fun stopSpeaking() {
        tts?.stop()
        isSpeaking = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }

    companion object {
        var appContext: android.content.Context? = null
    }
}

actual fun createSpeechManager(): SpeechManager {
    val ctx = AndroidSpeechManager.appContext
        ?: error("Call AndroidSpeechManager.appContext = context before createSpeechManager()")
    return AndroidSpeechManager(ctx)
}
