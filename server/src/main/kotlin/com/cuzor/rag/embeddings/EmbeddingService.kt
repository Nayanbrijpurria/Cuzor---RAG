package com.cuzor.rag.embeddings

// This file is responsible for generating embeddings for text using the EmbeddingClient(Bridge).

class EmbeddingService {

    suspend fun createEmbedding(
        text: String
    ): FloatArray {

        return EmbeddingClient.generateEmbedding(text)

    }

}