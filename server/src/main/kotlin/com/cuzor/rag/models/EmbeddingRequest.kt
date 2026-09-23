package com.cuzor.rag.models
// This file is used to define the EmbeddingRequest data class, which represents a request to generate an embedding for a given text.
// It contains a single property, text, which is the input text for which the embedding will be generated.

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingRequest(
    val text: String
)
