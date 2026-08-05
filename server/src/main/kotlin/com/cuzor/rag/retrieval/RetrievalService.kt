package com.cuzor.rag.retrieval

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.SearchResult
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.similarity.CosineSimilarity

class RetrievalService {

    private val embeddingService = EmbeddingService()

    suspend fun retrieve(query: String): List<SearchResult> {

        val queryEmbedding =
            embeddingService.createEmbedding(query)
        println("====================================")
        println("Starting Semantic Search")
        println("Query : $query")
        println("====================================")
        val storedEmbeddings =
            EmbeddingRepository.findAll()

        val results =
            mutableListOf<SearchResult>()

        storedEmbeddings.forEach {

            val score =

                CosineSimilarity.calculate(

                    queryEmbedding,

                    it.vector

                )

            results.add(

                SearchResult(

                    ticketId = it.ticketId,

                    similarity = score

                )

            )

        }
        println("Top Results")

        results
            .sortedByDescending { it.similarity }
            .take(5)
            .forEach {

                println("${it.ticketId} -> ${it.similarity}")

            }

        return results

            .sortedByDescending {

                it.similarity

            }
            .take(5)
    }

}