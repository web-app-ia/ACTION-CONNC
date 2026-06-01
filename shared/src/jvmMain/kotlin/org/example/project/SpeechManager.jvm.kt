package org.example.project

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import java.io.ByteArrayOutputStream
import java.io.File

class JvmSpeechManager : SpeechManager {
    override var isListening: Boolean = false
    override var isSpeaking: Boolean = false

    private var recordingThread: Thread? = null
    private var onResultCallback: ((String) -> Unit)? = null

    override fun startListening(onResult: (String) -> Unit) {
        onResultCallback = onResult
        isListening = true
        recordingThread = Thread {
            try {
                val format = AudioFormat(44100f, 16, 1, true, false)
                val info = DataLine.Info(TargetDataLine::class.java, format)
                val line = AudioSystem.getLine(info) as TargetDataLine
                line.open(format)
                line.start()
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (isListening && line.isOpen) {
                    bytesRead = line.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) out.write(buffer, 0, bytesRead)
                }
                line.stop()
                line.close()
                val audioData = out.toByteArray()
                if (audioData.isNotEmpty()) {
                    val tempFile = File.createTempFile("stt_", ".wav")
                    tempFile.writeBytes(audioData)
                    println("Audio recorded: ${tempFile.absolutePath}")
                }
            } catch (_: Exception) {}
        }
        recordingThread?.start()
    }

    override fun stopListening() {
        isListening = false
        recordingThread?.join(1000)
        recordingThread = null
    }

    override fun speak(text: String) {
        isSpeaking = true
        try {
            val process = ProcessBuilder(
                "powershell", "-Command",
                "Add-Type -AssemblyName System.Speech; " +
                "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "\$s.SelectVoiceByHints('Female', 'French'); " +
                "\$s.Speak('${text.replace("'", "''")}')"
            ).inheritIO().start()
            process.waitFor()
        } catch (_: Exception) {
            println("TTS not available on this platform")
        }
        isSpeaking = false
    }

    override fun stopSpeaking() {
        isSpeaking = false
    }
}

actual fun createSpeechManager(): SpeechManager = JvmSpeechManager()
