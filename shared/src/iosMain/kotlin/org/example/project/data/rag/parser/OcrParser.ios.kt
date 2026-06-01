package org.example.project.data.rag.parser

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Vision.VNDocumentCameraViewController
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRequestCompletionHandler
import platform.Vision.VNImageRequestHandler
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume

actual suspend fun ocrImage(bytes: ByteArray, fileName: String): String {
    return try {
        val request = VNRecognizeTextRequest()
        request.setRecognitionLevel(platform.Vision.VNRequestTextRecognitionLevelAccurate)
        request.setUsesLanguageCorrection(true)
        request.setRecognitionLanguages(listOf("fr-FR"))

        val handler = VNImageRequestHandler(bytes, mapOf<Any?, Any?>())
        val results = handler.performRequests(listOf(request), null)

        val observations = request.results ?: return "[OCR iOS] Aucun texte détecté"
        val text = observations.joinToString("\n") { obs ->
            (obs as? platform.Vision.VNRecognizedTextObservation)?.topCandidates(1)
                ?.firstOrNull()?.string ?: ""
        }
        text.ifBlank { "[OCR iOS] Aucun texte détecté" }
    } catch (e: Exception) {
        "[OCR iOS] Erreur: ${e.message}"
    }
}
