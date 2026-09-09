package com.cuzor.rag.retrieval

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.RetrievalResponse
import com.cuzor.rag.models.RetrievedTicket
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository

class RetrievalService {

    private val embeddingService =
        EmbeddingService()


    suspend fun retrieve(
        query: String,
        limit: Int = 20
    ): RetrievalResponse {

        println("====================================")
        println("Starting PostgreSQL Vector Search")
        println("Query : $query")
        println("Requested Limit : $limit")
        println("====================================")


        // ==========================================
        // 1. Convert user query into embedding
        // ==========================================

        val queryEmbedding =
            embeddingService.createEmbedding(
                query
            )


        // ==========================================
        // 2. Search PostgreSQL using HNSW
        // ==========================================

        val results =
            EmbeddingRepository.search(

                queryVector =
                    queryEmbedding,

                threshold =
                    -1.0f,

                limit =
                    limit
            )


        println("Matching Results")


        val highConfidence =
            mutableListOf<RetrievedTicket>()


        val lowConfidence =
            mutableListOf<RetrievedTicket>()


        // ==========================================
        // 3. Fetch complete ticket information
        // ==========================================

        results.forEach { result ->

            val ticketId =
                result.first

            val similarity =
                result.second


            val confidence =
                if (similarity >= 0.75f) {

                    "HIGH CONFIDENCE"

                } else {

                    "LOW CONFIDENCE"
                }


            println(
                "$ticketId -> $similarity -> $confidence"
            )


            val ticket =
                TicketRepository.findById(
                    ticketId
                )


            if (ticket != null) {

                val retrievedTicket =
                    RetrievedTicket(

                        ticketId =
                            ticket.ticketId,

                        created =
                            ticket.created,

                        closedAt =
                            ticket.closedAt,

                        state =
                            ticket.state,

                        category =
                            ticket.category,

                        urgency =
                            ticket.urgency,

                        customerName =
                            ticket.customerName,

                        customerPhone =
                            ticket.customerPhone,

                        messageCount =
                            ticket.messageCount,

                        title =
                            ticket.title,

                        description =
                            ticket.description,

                        resolution =
                            ticket.resolution,

                        conversation =
                            ticket.conversation,

                        similarity =
                            similarity
                    )


                if (similarity >= 0.75f) {

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


        // ==========================================
        // 4. Print statistics
        // ==========================================

        println("====================================")

        println(
            "Requested Tickets : $limit"
        )

        println(
            "Returned Tickets  : ${highConfidence.size + lowConfidence.size}"
        )

        println(
            "High Confidence   : ${highConfidence.size}"
        )

        println(
            "Low Confidence    : ${lowConfidence.size}"
        )

        println("====================================")


        // ==========================================
        // 5. Return results to UI
        // ==========================================

        return RetrievalResponse(

            highConfidence =
                highConfidence,

            lowConfidence =
                lowConfidence
        )
    }
}