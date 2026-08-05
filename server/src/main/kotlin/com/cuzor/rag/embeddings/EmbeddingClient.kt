package com.cuzor.rag.embeddings

import com.cuzor.rag.models.EmbeddingRequest
import com.cuzor.rag.models.EmbeddingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object EmbeddingClient {

    private const val BASE_URL = "http://localhost:8000"

    private val client = HttpClient(CIO) {

        install(ContentNegotiation) {

            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )

        }

    }

    suspend fun generateEmbedding(
        text: String
    ): FloatArray {

        val response: EmbeddingResponse =
            client.post("$BASE_URL/embed") {

                contentType(ContentType.Application.Json)

                setBody(
                    EmbeddingRequest(text)
                )

            }.body()

        return response.embedding.toFloatArray()

    }

}