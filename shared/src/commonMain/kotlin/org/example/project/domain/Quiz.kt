package org.example.project.domain

import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    val subject: String? = null
)
