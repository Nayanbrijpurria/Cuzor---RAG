package com.cuzor.rag.retrieval

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.RetrievalResponse
import com.cuzor.rag.models.RetrievedTicket
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository

class RetrievalService {

    private val embeddingService = EmbeddingService()

    suspend fun retrieve(
        query: String
    ): RetrievalResponse {

        println("====================================")
        println("Starting PostgreSQL Vector Search")
        println("Query : $query")
        println("====================================")

        // 1. Convert user query into embedding
        val queryEmbedding =
            embeddingService.createEmbedding(query)

        // 2. PostgreSQL + pgvector performs similarity search
        //    We retrieve every ticket with similarity > 0.50
        val results =
            EmbeddingRepository.search(
                queryVector = queryEmbedding,
                threshold = 0.5f
            )

        println("Matching Results")

        val highConfidence =
            mutableListOf<RetrievedTicket>()

        val lowConfidence =
            mutableListOf<RetrievedTicket>()

        // 3. Fetch complete ticket information
        results.forEach { result ->

            val ticketId = result.first
            val similarity = result.second

            val confidence =
                if (similarity > 0.75f) {
                    "HIGH CONFIDENCE"
                } else {
                    "LOW CONFIDENCE"
                }

            println(
                "$ticketId -> $similarity -> $confidence"
            )

            val ticket =
                TicketRepository.findById(ticketId)

            if (ticket != null) {

                val retrievedTicket =
                    RetrievedTicket(

                        ticketId = ticket.ticketId,

                        title = ticket.title,

                        description = ticket.description,

                        resolution = ticket.resolution,

                        conversation = ticket.conversation,

                        similarity = similarity
                    )

                if (similarity > 0.75f) {

                    highConfidence.add(
                        retrievedTicket
                    )

                } else {

                    lowConfidence.add(
                        retrievedTicket
                    )
                }
            }
        }

        println("====================================")
        println("High Confidence Tickets : ${highConfidence.size}")
        println("Low Confidence Tickets  : ${lowConfidence.size}")
        println("====================================")

        // 4. Return both groups
        return RetrievalResponse(

            highConfidence = highConfidence,

            lowConfidence = lowConfidence
        )
    }
}