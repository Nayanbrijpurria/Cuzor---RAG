package com.cuzor.rag.embeddings

interface EmbeddingModel {

    suspend fun generateEmbedding(
        text: String
    ): FloatArray

}