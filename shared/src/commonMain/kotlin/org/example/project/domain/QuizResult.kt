package org.example.project.domain

data class QuizResult(
    val quizId: String,
    val studentId: String,
    val selectedAnswerIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long
)
