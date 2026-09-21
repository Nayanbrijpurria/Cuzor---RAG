package com.cuzor.rag.repository

import com.cuzor.rag.database.TicketTable
import com.cuzor.rag.models.Ticket
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object TicketRepository {

    /*
     * Save a new ticket.
     *
     * New tickets automatically start with:
     *
     * embedding_verified = 0
     *
     * This means:
     * "Ticket exists, but its embedding has not been generated yet."
     */
    fun save(ticket: Ticket) {

        try {

            transaction {

                TicketTable.insert {

                    it[ticketId] = ticket.ticketId

                    it[created] = ticket.created

                    it[closedAt] = ticket.closedAt

                    it[state] = ticket.state

                    it[category] = ticket.category

                    it[urgency] = ticket.urgency

                    it[customerName] = ticket.customerName

                    it[customerPhone] = ticket.customerPhone

                    it[messageCount] = ticket.messageCount

                    it[title] = ticket.title

                    it[description] = ticket.description

                    it[resolution] = ticket.resolution

                    it[conversation] = ticket.conversation

                    // New ticket → embedding is pending
                    it[embeddingVerified] = 0
                }
            }

        } catch (e: Exception) {

            if (e.toString().contains("duplicate key")) {

                // Silently ignore duplicate key errors

            } else {

                throw e
            }
        }
    }


    /*
     * Check whether a ticket already exists.
     */
    fun exists(ticketId: String): Boolean {

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.ticketId eq ticketId
                }
                .count() > 0
        }
    }


    /*
     * Get total number of tickets.
     */
    fun count(): Long {

        return transaction {

            TicketTable
                .selectAll()
                .count()
        }
    }


    /*
     * Find tickets whose embeddings have not been generated yet.
     *
     * embedding_verified = 0
     */
    fun findPendingEmbeddingTickets(limit: Int = 50): List<Ticket> {

        val safeLimit = limit.coerceIn(1, 500)

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.embeddingVerified eq 0
                }
                .limit(safeLimit)
                .map {

                    Ticket(

                        ticketId = it[TicketTable.ticketId],

                        created = it[TicketTable.created],

                        closedAt = it[TicketTable.closedAt],

                        state = it[TicketTable.state],

                        category = it[TicketTable.category],

                        urgency = it[TicketTable.urgency],

                        customerName = it[TicketTable.customerName],

                        customerPhone = it[TicketTable.customerPhone],

                        messageCount = it[TicketTable.messageCount],

                        title = it[TicketTable.title],

                        description = it[TicketTable.description],

                        resolution = it[TicketTable.resolution],

                        conversation = it[TicketTable.conversation]
                    )
                }
        }
    }


    /*
     * Mark embedding as successfully generated.
     *
     * 0 → 1
     */
    fun markEmbeddingVerified(ticketId: String) {

        transaction {

            TicketTable.update(
                where = {
                    TicketTable.ticketId eq ticketId
                }
            ) {

                it[embeddingVerified] = 1
            }
        }
    }


    /*
     * Reset embedding status.
     *
     * 1 → 0
     *
     * This will be used when an existing ticket
     * is updated and therefore needs a new embedding.
     */
    fun markEmbeddingPending(ticketId: String) {

        transaction {

            TicketTable.update(
                where = {
                    TicketTable.ticketId eq ticketId
                }
            ) {

                it[embeddingVerified] = 0
            }
        }
    }


    /*
     * Keyword search.
     */
    fun keywordSearch(query: String): List<Ticket> {

        println("Searching database for: $query")

        return transaction {

            TicketTable
                .selectAll()
                .map {

                    Ticket(

                        ticketId = it[TicketTable.ticketId],

                        created = it[TicketTable.created],

                        closedAt = it[TicketTable.closedAt],

                        state = it[TicketTable.state],

                        category = it[TicketTable.category],

                        urgency = it[TicketTable.urgency],

                        customerName = it[TicketTable.customerName],

                        customerPhone = it[TicketTable.customerPhone],

                        messageCount = it[TicketTable.messageCount],

                        title = it[TicketTable.title],

                        description = it[TicketTable.description],

                        resolution = it[TicketTable.resolution],

                        conversation = it[TicketTable.conversation]
                    )
                }
        }
    }


    /*
     * Get first 5 tickets.
     */
    fun findTop5(): List<Ticket> {

        return transaction {

            TicketTable
                .selectAll()
                .limit(5)
                .map {

                    Ticket(

                        ticketId = it[TicketTable.ticketId],

                        created = it[TicketTable.created],

                        closedAt = it[TicketTable.closedAt],

                        state = it[TicketTable.state],

                        category = it[TicketTable.category],

                        urgency = it[TicketTable.urgency],

                        customerName = it[TicketTable.customerName],

                        customerPhone = it[TicketTable.customerPhone],

                        messageCount = it[TicketTable.messageCount],

                        title = it[TicketTable.title],

                        description = it[TicketTable.description],

                        resolution = it[TicketTable.resolution],

                        conversation = it[TicketTable.conversation]
                    )
                }
        }
    }


    /*
     * Find a ticket using its ID.
     */
    fun findById(ticketId: String): Ticket? {

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.ticketId eq ticketId
                }
                .map {

                    Ticket(

                        ticketId = it[TicketTable.ticketId],

                        created = it[TicketTable.created],

                        closedAt = it[TicketTable.closedAt],

                        state = it[TicketTable.state],

                        category = it[TicketTable.category],

                        urgency = it[TicketTable.urgency],

                        customerName = it[TicketTable.customerName],

                        customerPhone = it[TicketTable.customerPhone],

                        messageCount = it[TicketTable.messageCount],

                        title = it[TicketTable.title],

                        description = it[TicketTable.description],

                        resolution = it[TicketTable.resolution],

                        conversation = it[TicketTable.conversation]
                    )
                }
                .firstOrNull()
        }
    }
}