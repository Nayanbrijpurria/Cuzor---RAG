package com.cuzor.rag.embeddings

class EmbeddingService {

    suspend fun createEmbedding(
        text: String
    ): FloatArray {

        return EmbeddingClient.generateEmbedding(text)

    }

}