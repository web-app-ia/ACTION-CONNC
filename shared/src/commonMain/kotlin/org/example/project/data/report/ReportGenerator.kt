package org.example.project.data.report

import org.example.project.core.currentTimeMillis
import org.example.project.domain.Skill
import org.example.project.domain.Student

data class StudyReport(
    val studentName: String,
    val level: String,
    val generatedAt: Long,
    val totalStudyMinutes: Long,
    val averageScore: Double,
    val skills: List<Skill>,
    val recommendations: List<String>
)

object ReportGenerator {

    fun generateTextReport(report: StudyReport): String {
        val sb = StringBuilder()
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("       TUTORIA'IAD - Bilan de Compétences")
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine()
        sb.appendLine("Apprenant : ${report.studentName}")
        sb.appendLine("Niveau     : ${report.level}")
        sb.appendLine("Date       : ${formatTimestamp(report.generatedAt)}")
        sb.appendLine()
        sb.appendLine("---------------------------------------")
        sb.appendLine("RÉSULTATS GLOBAUX")
        sb.appendLine("---------------------------------------")
        sb.appendLine("Temps d'étude total : ${report.totalStudyMinutes} minutes")
        sb.appendLine("Score moyen         : ${"%.1f".format(report.averageScore)}%")
        sb.appendLine()
        sb.appendLine("---------------------------------------")
        sb.appendLine("COMPÉTENCES DÉTAILLÉES")
        sb.appendLine("---------------------------------------")
        report.skills.forEach { skill ->
            val bar = buildProgressBar(skill.proficiency)
            sb.appendLine("${skill.name} (${skill.category})")
            sb.appendLine("  $bar ${"%.0f".format(skill.proficiency)}%")
        }
        sb.appendLine()
        if (report.recommendations.isNotEmpty()) {
            sb.appendLine("---------------------------------------")
            sb.appendLine("RECOMMANDATIONS")
            sb.appendLine("---------------------------------------")
            report.recommendations.forEach { rec ->
                sb.appendLine("• $rec")
            }
            sb.appendLine()
        }
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("Rapport généré par TUTORIA'IAD")
        return sb.toString()
    }

    private fun buildProgressBar(percent: Double): String {
        val filled = (percent / 100 * 20).toInt().coerceIn(0, 20)
        return "[" + "█".repeat(filled) + "░".repeat(20 - filled) + "]"
    }

    private fun formatTimestamp(millis: Long): String {
        val seconds = millis / 1000
        val minutes = (seconds / 60) % 60
        val hours = (seconds / 3600) % 24
        val days = seconds / 86400
        return "${days}j ${hours}h ${minutes}m"
    }
}
