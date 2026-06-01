package org.example.project.data.sync

import kotlinx.serialization.Serializable
import org.example.project.data.cache.*
import org.example.project.data.cache.ChatMessageEntity
import org.example.project.data.cache.DocumentEntity
import org.example.project.data.cache.QuizResultEntity
import org.example.project.data.cache.SkillEntity
import org.example.project.data.cache.StudentEntity
import org.example.project.data.cache.StudySessionEntity

@Serializable
data class SyncPayload(
    val deviceId: String,
    val timestamp: Long,
    val students: List<StudentEntity> = emptyList(),
    val sessions: List<StudySessionEntity> = emptyList(),
    val messages: List<ChatMessageEntity> = emptyList(),
    val quizResults: List<QuizResultEntity> = emptyList(),
    val skills: List<SkillEntity> = emptyList(),
    val documents: List<DocumentEntity> = emptyList()
)

@Serializable
data class SyncResponse(
    val success: Boolean,
    val message: String = "",
    val serverTimestamp: Long = 0
)
