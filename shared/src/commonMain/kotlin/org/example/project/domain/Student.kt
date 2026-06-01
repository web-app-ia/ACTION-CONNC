package org.example.project.domain

data class Student(
    val id: String,
    val name: String,
    val level: String,
    val studyTimeMinutes: Long = 0,
    val totalQuizzesTaken: Int = 0,
    val averageScore: Double = 0.0
)
