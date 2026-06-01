package org.example.project.data.rag.parser

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

actual suspend fun ocrImage(bytes: ByteArray, fileName: String): String {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val task = recognizer.process(image)
        val result = java.util.concurrent.CompletableFuture<String>()
        task.addOnSuccessListener { visionText ->
            result.complete(visionText.text)
        }.addOnFailureListener { e ->
            result.completeExceptionally(e)
        }
        result.get()
    } catch (e: Exception) {
        "[OCR Android] Erreur: ${e.message}"
    }
}
