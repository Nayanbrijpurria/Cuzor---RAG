package com.cuzor.rag.retrieval

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.RetrievalResponse
import com.cuzor.rag.models.RetrievedTicket
import com.cuzor.rag.models.SearchResult
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository
import com.cuzor.rag.similarity.CosineSimilarity

class RetrievalService {

    private val embeddingService = EmbeddingService()

    suspend fun retrieve(query: String): RetrievalResponse {

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

        val highConfidenceResults =
            results
                .filter { it.similarity > 0.75f }
                .sortedByDescending { it.similarity }

        val lowConfidenceResults =
            results
                .filter {
                    it.similarity > 0.50f &&
                            it.similarity <= 0.75f
                }
                .sortedByDescending { it.similarity }

        println("====================================")
        println("HIGH CONFIDENCE RESULTS (> 0.75)")
        println("====================================")

        highConfidenceResults.forEach {
            println("${it.ticketId} -> ${it.similarity}")
        }

        println("====================================")
        println("LOW CONFIDENCE RESULTS (> 0.50 and <= 0.75)")
        println("====================================")

        lowConfidenceResults.forEach {
            println("${it.ticketId} -> ${it.similarity}")
        }

        val highConfidenceTickets =
            getTickets(highConfidenceResults)

        val lowConfidenceTickets =
            getTickets(lowConfidenceResults)

        return RetrievalResponse(
            highConfidence = highConfidenceTickets,
            lowConfidence = lowConfidenceTickets
        )
    }

    private fun getTickets(
        results: List<SearchResult>
    ): List<RetrievedTicket> {

        val retrievedTickets =
            mutableListOf<RetrievedTicket>()

        results.forEach { result ->

            val ticket =
                TicketRepository.findById(result.ticketId)

            if (ticket != null) {

                retrievedTickets.add(
                    RetrievedTicket(
                        ticketId = ticket.ticketId,
                        title = ticket.title,
                        description = ticket.description,
                        resolution = ticket.resolution,
                        conversation = ticket.conversation,
                        similarity = result.similarity
                    )
                )
            }
        }

        return retrievedTickets
    }
}