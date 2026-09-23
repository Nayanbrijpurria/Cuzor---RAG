package com.cuzor.rag.models

// this file contains the response from the embedding endpoint (converting object format JSON ).

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingResponse(
    val embedding: List<Float>
)









