package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingHealth(
    val total: Long,
    val pending: Long,
    val embedded: Long,
    val failed: Long,
    val skipped: Long
)