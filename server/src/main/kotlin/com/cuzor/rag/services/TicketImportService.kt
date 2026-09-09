package com.cuzor.rag.services

import com.cuzor.rag.csv.CsvReader
import com.cuzor.rag.repository.EmbeddingRepository
import com.cuzor.rag.repository.TicketRepository

class TicketImportService {

    private val ticketEmbeddingService =
        TicketEmbeddingService()


    suspend fun importCsv(
        filePath: String
    ): ImportResult {

        println("===================================")
        println("Starting Ticket Import")
        println("CSV File : $filePath")
        println("===================================")


        // 1. Read CSV
        val tickets =
            CsvReader.read(filePath)

        println(
            "Tickets Found : ${tickets.size}"
        )


        var newTickets = 0
        var existingTickets = 0
        var embeddingsGenerated = 0
        var embeddingsSkipped = 0


        // 2. Process every ticket
        for (ticket in tickets) {

            println("-----------------------------------")
            println(
                "Processing Ticket : ${ticket.ticketId}"
            )


            val ticketExists =
                TicketRepository.exists(
                    ticket.ticketId
                )


            val embeddingExists =
                EmbeddingRepository.exists(
                    ticket.ticketId
                )


            // =====================================
            // CASE 1
            // Ticket and embedding already exist
            // =====================================

            if (ticketExists && embeddingExists) {

                println(
                    "Skipping existing ticket + embedding : " +
                            ticket.ticketId
                )

                existingTickets++
                embeddingsSkipped++

                continue
            }


            // =====================================
            // CASE 2
            // Ticket doesn't exist
            // =====================================

            if (!ticketExists) {

                println(
                    "New ticket found : ${ticket.ticketId}"
                )

                TicketRepository.save(ticket)

                newTickets++

                println(
                    "Ticket saved : ${ticket.ticketId}"
                )
            }


            // =====================================
            // CASE 3
            // Embedding doesn't exist
            // =====================================

            if (!embeddingExists) {

                println(
                    "Generating embedding using Python : " +
                            ticket.ticketId
                )

                ticketEmbeddingService.save(ticket)

                embeddingsGenerated++

                println(
                    "Embedding saved : ${ticket.ticketId}"
                )
            }
        }


        println("===================================")
        println("Ticket Import Finished")
        println("===================================")

        println(
            "New Tickets            : $newTickets"
        )

        println(
            "Existing Tickets       : $existingTickets"
        )

        println(
            "Embeddings Generated   : $embeddingsGenerated"
        )

        println(
            "Embeddings Skipped     : $embeddingsSkipped"
        )

        println("===================================")


        return ImportResult(

            totalTickets = tickets.size,

            newTickets = newTickets,

            existingTickets = existingTickets,

            embeddingsGenerated =
                embeddingsGenerated,

            embeddingsSkipped =
                embeddingsSkipped
        )
    }
}


data class ImportResult(

    val totalTickets: Int,

    val newTickets: Int,

    val existingTickets: Int,

    val embeddingsGenerated: Int,

    val embeddingsSkipped: Int
)