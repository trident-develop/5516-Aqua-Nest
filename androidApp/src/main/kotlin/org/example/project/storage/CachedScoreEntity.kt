package org.example.project.storage

data class CachedScoreEntity(
    val id: Int = 1,
    val score: String,
    val createdAt: Long
)