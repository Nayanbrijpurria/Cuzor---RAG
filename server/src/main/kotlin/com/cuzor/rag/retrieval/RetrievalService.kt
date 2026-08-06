package com.cuzor.rag.retrieval

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.RetrievedTicket
import com.cuzor.rag.models.SearchResult
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository
import com.cuzor.rag.similarity.CosineSimilarity

class RetrievalService {

    private val embeddingService = EmbeddingService()

    suspend fun retrieve(query: String): List<RetrievedTicket> {

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

        val topFive =
            results
                .sortedByDescending { it.similarity }
                .take(5)

        println("Top Results")

        topFive.forEach {

            println("${it.ticketId} -> ${it.similarity}")

        }

        val retrievedTickets =
            mutableListOf<RetrievedTicket>()

        topFive.forEach { result ->

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