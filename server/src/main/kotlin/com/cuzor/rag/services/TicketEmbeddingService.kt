package com.cuzor.rag.services

import com.cuzor.rag.embeddings.EmbeddingService
import com.cuzor.rag.models.EmbeddingVector
import com.cuzor.rag.models.Ticket
import com.cuzor.rag.repository.EmbeddingRepository

class TicketEmbeddingService {

    private val embeddingService = EmbeddingService()

    suspend fun generate(ticket: Ticket): EmbeddingVector {

        val text = buildString {

            append(ticket.title)
            append("\n")

            append(ticket.description)
            append("\n")

            append(ticket.resolution)
            append("\n")

            append(ticket.conversation)
        }

        println("Generating embedding for Ticket : ${ticket.ticketId}")

        val vector = embeddingService.createEmbedding(text)

        return EmbeddingVector(

            ticketId = ticket.ticketId,

            vector = vector
        )
    }

    suspend fun save(ticket: Ticket) {

        val embedding = generate(ticket)

        EmbeddingRepository.save(embedding)
    }
}