package com.cuzor.rag.models

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingRequest(
    val text: String
)