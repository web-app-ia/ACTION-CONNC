package org.example.project.domain

import kotlinx.serialization.Serializable

@Serializable
data class Flashcard(
    val id: String,
    val front: String,
    val back: String,
    val subject: String? = null,
    val masteryLevel: Int = 0
)
