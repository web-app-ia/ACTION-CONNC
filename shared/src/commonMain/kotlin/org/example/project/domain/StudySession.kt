package org.example.project.domain

data class StudySession(
    val id: String,
    val studentId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val subject: String? = null,
    val messageCount: Int = 0
)
