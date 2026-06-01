package org.example.project.domain

data class Skill(
    val id: String,
    val name: String,
    val category: String,
    val proficiency: Double = 0.0,
    val lastUpdated: Long = 0
)
