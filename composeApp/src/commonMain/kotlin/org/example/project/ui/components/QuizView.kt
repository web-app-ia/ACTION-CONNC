package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.core.currentTimeMillis
import org.example.project.domain.Quiz
import org.example.project.domain.QuizResult

@Composable
fun QuizView(
    quiz: Quiz,
    onAnswer: (QuizResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quiz",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = quiz.question,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(12.dp))

            quiz.options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                val isCorrect = hasSubmitted && index == quiz.correctAnswerIndex
                val isWrong = hasSubmitted && isSelected && index != quiz.correctAnswerIndex

                val buttonColors = when {
                    isCorrect -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                    isWrong -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                    isSelected -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                    else -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (!hasSubmitted) {
                            selectedIndex = index
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = buttonColors,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (selectedIndex != null && !hasSubmitted) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        hasSubmitted = true
                        onAnswer(
                            QuizResult(
                                quizId = quiz.id,
                                studentId = "",
                                selectedAnswerIndex = selectedIndex!!,
                                isCorrect = selectedIndex == quiz.correctAnswerIndex,
                                timestamp = currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Soumettre")
                }
            }

            if (hasSubmitted && quiz.explanation != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = quiz.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
