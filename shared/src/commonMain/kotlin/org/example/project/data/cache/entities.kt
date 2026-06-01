package org.example.project.data.cache

import kotlinx.serialization.Serializable

@Serializable
data class StudentEntity(
    val id: String,
    val name: String,
    val level: String,
    val studyTimeMinutes: Long = 0,
    val totalQuizzesTaken: Int = 0,
    val averageScore: Double = 0.0
)

@Serializable
data class StudySessionEntity(
    val id: String,
    val studentId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val subject: String? = null,
    val messageCount: Int = 0
)

@Serializable
data class ChatMessageEntity(
    val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val sourceFile: String? = null,
    val fileType: String? = null,
    val pageNumber: Int? = null,
    val localUri: String? = null,
    val mcpWidgetData: String? = null
)

@Serializable
data class QuizResultEntity(
    val id: String,
    val sessionId: String,
    val question: String,
    val options: String,
    val correctAnswerIndex: Int,
    val selectedAnswerIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long
)

@Serializable
data class SkillEntity(
    val id: String,
    val studentId: String,
    val name: String,
    val category: String,
    val proficiency: Double = 0.0,
    val lastUpdated: Long = 0
)

@Serializable
data class DocumentEntity(
    val id: String,
    val fileName: String,
    val fileType: String,
    val localUri: String,
    val pageCount: Int? = null,
    val indexedAt: Long = 0
)
