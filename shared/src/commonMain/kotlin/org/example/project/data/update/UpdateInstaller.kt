package org.example.project.data.update

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateInstaller(
    private val httpClient: HttpClient
) {
    suspend fun downloadUpdate(
        url: String,
        onProgress: (Float) -> Unit
    ): ByteArray {
        return withContext(Dispatchers.Default) {
            val response = httpClient.prepareGet(url) {
                header("User-Agent", "TUTORIA-IAD-Updater/1.0")
            }.execute()

            val channel = response.bodyAsChannel()
            val totalBytes = response.headers["Content-Length"]?.toLongOrNull() ?: -1L
            val buffer = mutableListOf<Byte>()
            val chunk = ByteArray(8192)
            var downloaded = 0L

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(chunk)
                if (bytesRead == -1) break
                repeat(bytesRead) { i -> buffer.add(chunk[i]) }
                downloaded += bytesRead
                if (totalBytes > 0) {
                    onProgress(downloaded.toFloat() / totalBytes)
                }
            }

            buffer.toByteArray()
        }
    }

    suspend fun install(bytes: ByteArray, fileName: String): Boolean {
        return platformInstall(bytes, fileName)
    }
}

expect suspend fun platformInstall(bytes: ByteArray, fileName: String): Boolean
