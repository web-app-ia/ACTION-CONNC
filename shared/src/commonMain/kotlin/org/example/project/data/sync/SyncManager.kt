package org.example.project.data.sync

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.currentTimeMillis
import org.example.project.data.cache.*
import org.example.project.data.network.KtorClientFactory

class SyncManager(
    private val database: LocalDatabase,
    private val cloudClient: HttpClient = KtorClientFactory.createCloudClient(),
    private val serverUrl: String = "https://sync.tutoriaiad.app",
    private val deviceId: String = "device_${currentTimeMillis()}"
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private var syncJob: Job? = null
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startPeriodicSync(intervalMinutes: Long = 15) {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                try {
                    sync()
                } catch (_: Exception) {}
                delay(intervalMinutes * 60 * 1000)
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun sync(): SyncResponse {
        return try {
            val payload = buildPayload()
            val body = cloudClient.post("$serverUrl/api/sync") {
                contentType(ContentType.Application.Json)
                header("X-Device-ID", deviceId)
                header("User-Agent", "TUTORIA-IAD-Sync/1.0")
                setBody(json.encodeToString(payload))
            }.bodyAsText()
            json.decodeFromString<SyncResponse>(body)
        } catch (e: Exception) {
            SyncResponse(success = false, message = e.message ?: "Sync failed")
        }
    }

    private suspend fun buildPayload(): SyncPayload {
        return SyncPayload(
            deviceId = deviceId,
            timestamp = currentTimeMillis(),
            students = database.getAllStudents(),
            documents = database.getAllDocuments()
        )
    }

    fun shutdown() {
        stopSync()
        scope.cancel()
    }
}
