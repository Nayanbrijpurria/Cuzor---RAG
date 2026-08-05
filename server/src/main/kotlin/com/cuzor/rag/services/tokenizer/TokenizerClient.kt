package com.cuzor.rag.service.tokenizer

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object TokenizerClient {

    private val client = HttpClient(CIO) {

        install(ContentNegotiation) {

            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )

        }

    }

    suspend fun tokenize(
        text: String
    ): TokenizerResponse {

        return client.post(
            "http://127.0.0.1:8000/tokenize"
        ) {

            contentType(ContentType.Application.Json)

            setBody(
                TokenizerRequest(text)
            )

        }.body()

    }

}

@Serializable
data class TokenizerRequest(

    val text: String

)

@Serializable
data class TokenizerResponse(

    val input_ids: List<Long>,

    val attention_mask: List<Long>

)