package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingResponse(
    val embedding: List<Float>
)