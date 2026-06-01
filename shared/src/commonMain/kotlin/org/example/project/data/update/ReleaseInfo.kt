package org.example.project.data.update

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    val tag_name: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String = "",
    val browser_download_url: String = "",
    val size: Long = 0
)

data class ReleaseInfo(
    val version: AppVersion,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileName: String,
    val fileSize: Long
)
