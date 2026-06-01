package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ProgressData(
    val label: String,
    val current: Int,
    val total: Int,
    val unit: String = ""
)

@Composable
fun ProgressTrackerView(
    progress: ProgressData,
    modifier: Modifier = Modifier
) {
    val progressFloat = if (progress.total > 0)
        (progress.current.toFloat() / progress.total).coerceIn(0f, 1f)
    else 0f

    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Progression",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = progress.label,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressFloat },
                modifier = Modifier.fillMaxWidth().height(12.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${progress.current} / ${progress.total} ${progress.unit}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
