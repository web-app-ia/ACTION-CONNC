package org.example.project.data.update

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class UpdateChecker(
    private val httpClient: HttpClient,
    private val repoOwner: String = "web-app-ia",
    private val repoName: String = "ACTION-CONNC",
    private val currentVersion: AppVersion = AppVersion.CURRENT
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun checkForUpdate(): UpdateResult {
        return try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val body = httpClient.get(url) {
                header("Accept", "application/vnd.github.v3+json")
                header("User-Agent", "TUTORIA-IAD-Updater/1.0")
            }.bodyAsText()

            val release = json.decodeFromString<GitHubRelease>(body)
            val remoteVersion = AppVersion.parse(release.tag_name)

            if (remoteVersion > currentVersion) {
                val asset = selectAsset(release.assets)
                if (asset != null) {
                    UpdateResult.UpdateAvailable(
                        ReleaseInfo(
                            version = remoteVersion,
                            releaseName = release.name.ifBlank { release.tag_name },
                            releaseNotes = release.body,
                            downloadUrl = asset.browser_download_url,
                            fileName = asset.name,
                            fileSize = asset.size
                        )
                    )
                } else {
                    UpdateResult.NoAsset
                }
            } else {
                UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateResult.Error("Échec de la vérification : ${e.message}")
        }
    }

    private fun selectAsset(assets: List<GitHubAsset>): GitHubAsset? {
        val osName = platformOsName()
        return when {
            osName.contains("win", ignoreCase = true) ->
                assets.firstOrNull { it.name.endsWith(".msi") }
            osName.contains("android", ignoreCase = true) ->
                assets.firstOrNull { it.name.endsWith(".apk") }
            osName.contains("mac", ignoreCase = true) || osName.contains("darwin", ignoreCase = true) ->
                assets.firstOrNull { it.name.endsWith(".dmg") }
            osName.contains("linux", ignoreCase = true) ->
                assets.firstOrNull { it.name.endsWith(".deb") }
            osName.contains("ios", ignoreCase = true) ->
                assets.firstOrNull { it.name.endsWith(".ipa") }
            else -> assets.firstOrNull()
        }
    }
}

sealed class UpdateResult {
    data object UpToDate : UpdateResult()
    data class UpdateAvailable(val release: ReleaseInfo) : UpdateResult()
    data object NoAsset : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

expect fun platformOsName(): String
