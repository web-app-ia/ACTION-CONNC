package org.example.project

import platform.AVFoundation.AVSpeechSynthesizer
import platform.AVFoundation.AVSpeechUtterance
import platform.Foundation.NSLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognitionResult
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionCategoryOptions
import platform.AVFAudio.AVAudioSessionModeDefault

class IosSpeechManager : SpeechManager {
    override var isListening: Boolean = false
    override var isSpeaking: Boolean = false

    private val synthesizer = AVSpeechSynthesizer()
    private var recognitionTask: SFSpeechRecognitionTask? = null
    private var onResultCallback: ((String) -> Unit)? = null

    override fun startListening(onResult: (String) -> Unit) {
        onResultCallback = onResult
        val recognizer = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = "fr-FR"))
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryRecord, AVAudioSessionCategoryOptions())
        audioSession.setMode(AVAudioSessionModeDefault)
        audioSession.setActive(true)

        val request = SFSpeechAudioBufferRecognitionRequest()
        recognitionTask = recognizer?.recognitionTaskWithRequest(request) { result: SFSpeechRecognitionResult?, _ ->
            result?.let {
                isListening = !it.isFinal
                if (it.isFinal) {
                    it.bestTranscription.formattedString.let { text ->
                        onResultCallback?.invoke(text)
                    }
                }
            }
        }
    }

    override fun stopListening() {
        recognitionTask?.cancel()
        recognitionTask = null
        isListening = false
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryPlayback, AVAudioSessionCategoryOptions())
    }

    override fun speak(text: String) {
        isSpeaking = true
        val utterance = AVSpeechUtterance(text)
        utterance.voice = platform.AVFoundation.AVSpeechSynthesisVoice(identifier = "fr-FR")
        synthesizer.speakUtterance(utterance)
        isSpeaking = false
    }

    override fun stopSpeaking() {
        synthesizer.stopSpeakingAtBoundary(platform.AVFoundation.AVSpeechBoundaryImmediate)
        isSpeaking = false
    }
}

actual fun createSpeechManager(): SpeechManager = IosSpeechManager()
