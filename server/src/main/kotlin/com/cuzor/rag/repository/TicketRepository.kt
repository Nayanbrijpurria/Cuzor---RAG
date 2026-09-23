package com.cuzor.rag.repository

import com.cuzor.rag.database.TicketTable
import com.cuzor.rag.models.EmbeddingStatus
import com.cuzor.rag.models.Ticket
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

// This file is responsible for interacting with the database for ticket-related operations.

object TicketRepository {

    /*
     * =========================================================
     * SAVE NEW TICKET
     * =========================================================
     *
     * New tickets always start as:
     *
     * PENDING
     * attempts = 0
     *
     * Meaning:
     * "The ticket exists, but embedding has not been generated."
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

                    // New ticket → PENDING
                    it[embeddingVerified] =
                        EmbeddingStatus.PENDING.code

                    // No embedding attempt yet
                    it[embeddingAttempts] = 0
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
     * =========================================================
     * CHECK WHETHER TICKET EXISTS
     * =========================================================
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
     * =========================================================
     * GET TOTAL NUMBER OF TICKETS
     * =========================================================
     */

    fun count(): Long {

        return transaction {

            TicketTable
                .selectAll()
                .count()
        }
    }


    /*
     * =========================================================
     * FIND PENDING EMBEDDING TICKETS
     * =========================================================
     *
     * Only tickets with:
     *
     * embedding_verified = 0
     *
     * are returned.
     *
     * FAILED tickets (2) are therefore ignored.
     */

    fun findPendingEmbeddingTickets(
        limit: Int = 50
    ): List<Ticket> {

        val safeLimit =
            limit.coerceIn(1, 500)

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.embeddingVerified eq
                            EmbeddingStatus.PENDING.code
                }
                .limit(safeLimit)
                .map {

                    Ticket(

                        ticketId =
                            it[TicketTable.ticketId],

                        created =
                            it[TicketTable.created],

                        closedAt =
                            it[TicketTable.closedAt],

                        state =
                            it[TicketTable.state],

                        category =
                            it[TicketTable.category],

                        urgency =
                            it[TicketTable.urgency],

                        customerName =
                            it[TicketTable.customerName],

                        customerPhone =
                            it[TicketTable.customerPhone],

                        messageCount =
                            it[TicketTable.messageCount],

                        title =
                            it[TicketTable.title],

                        description =
                            it[TicketTable.description],

                        resolution =
                            it[TicketTable.resolution],

                        conversation =
                            it[TicketTable.conversation],

                        embeddingVerified =
                            it[TicketTable.embeddingVerified],

                        embeddingAttempts =
                            it[TicketTable.embeddingAttempts]
                    )
                }
        }
    }


    /*
     * =========================================================
     * MARK EMBEDDING AS SUCCESSFUL
     * =========================================================
     *
     * PENDING → EMBEDDED
     *
     * 0 → 1
     *
     * We do NOT reset attempts here.
     * Attempts tell us how many times we tried.
     */

    fun markEmbeddingVerified(
        ticketId: String
    ) {

        transaction {

            TicketTable.update(
                where = {
                    TicketTable.ticketId eq ticketId
                }
            ) {

                it[embeddingVerified] =
                    EmbeddingStatus.EMBEDDED.code
            }
        }
    }


    /*
     * =========================================================
     * INCREMENT EMBEDDING ATTEMPTS
     * =========================================================
     *
     * Example:
     *
     * 0 → 1
     * 1 → 2
     */

    fun incrementEmbeddingAttempts(
        ticketId: String
    ): Int {

        return transaction {

            val currentAttempts =
                TicketTable
                    .selectAll()
                    .where {
                        TicketTable.ticketId eq ticketId
                    }
                    .map {
                        it[TicketTable.embeddingAttempts]
                    }
                    .firstOrNull()
                    ?: 0

            val newAttempts =
                (currentAttempts + 1)
                    .coerceAtMost(2)

            TicketTable.update(
                where = {
                    TicketTable.ticketId eq ticketId
                }
            ) {

                it[embeddingAttempts] =
                    newAttempts
            }

            newAttempts
        }
    }


    /*
     * =========================================================
     * MARK EMBEDDING AS FAILED
     * =========================================================
     *
     * PENDING → FAILED
     *
     * 0 → 2
     *
     * This should only happen after the second failed attempt.
     */

    fun markEmbeddingFailed(
        ticketId: String
    ) {

        transaction {

            TicketTable.update(
                where = {
                    TicketTable.ticketId eq ticketId
                }
            ) {

                it[embeddingVerified] =
                    EmbeddingStatus.FAILED.code
            }
        }
    }


    /*
     * =========================================================
     * RESET EMBEDDING
     * =========================================================
     *
     * Used when an existing ticket is updated.
     *
     * FAILED / EMBEDDED / PENDING
     *             ↓
     *          PENDING
     *
     * Attempts are also reset because this is effectively
     * a new embedding job for the updated ticket.
     */

    fun markEmbeddingPending(
        ticketId: String
    ) {

        transaction {

            TicketTable.update(
                where = {
                    TicketTable.ticketId eq ticketId
                }
            ) {

                it[embeddingVerified] =
                    EmbeddingStatus.PENDING.code

                it[embeddingAttempts] =
                    0
            }
        }
    }


    /*
     * =========================================================
     * KEYWORD SEARCH
     * =========================================================
     */

    fun keywordSearch(
        query: String
    ): List<Ticket> {

        println(
            "Searching database for: $query"
        )

        return transaction {

            TicketTable
                .selectAll()
                .map {

                    Ticket(

                        ticketId =
                            it[TicketTable.ticketId],

                        created =
                            it[TicketTable.created],

                        closedAt =
                            it[TicketTable.closedAt],

                        state =
                            it[TicketTable.state],

                        category =
                            it[TicketTable.category],

                        urgency =
                            it[TicketTable.urgency],

                        customerName =
                            it[TicketTable.customerName],

                        customerPhone =
                            it[TicketTable.customerPhone],

                        messageCount =
                            it[TicketTable.messageCount],

                        title =
                            it[TicketTable.title],

                        description =
                            it[TicketTable.description],

                        resolution =
                            it[TicketTable.resolution],

                        conversation =
                            it[TicketTable.conversation],

                        embeddingVerified =
                            it[TicketTable.embeddingVerified],

                        embeddingAttempts =
                            it[TicketTable.embeddingAttempts]
                    )
                }
        }
    }


    /*
     * =========================================================
     * GET FIRST 5 TICKETS
     * =========================================================
     */

    fun findTop5(): List<Ticket> {

        return transaction {

            TicketTable
                .selectAll()
                .limit(5)
                .map {

                    Ticket(

                        ticketId =
                            it[TicketTable.ticketId],

                        created =
                            it[TicketTable.created],

                        closedAt =
                            it[TicketTable.closedAt],

                        state =
                            it[TicketTable.state],

                        category =
                            it[TicketTable.category],

                        urgency =
                            it[TicketTable.urgency],

                        customerName =
                            it[TicketTable.customerName],

                        customerPhone =
                            it[TicketTable.customerPhone],

                        messageCount =
                            it[TicketTable.messageCount],

                        title =
                            it[TicketTable.title],

                        description =
                            it[TicketTable.description],

                        resolution =
                            it[TicketTable.resolution],

                        conversation =
                            it[TicketTable.conversation],

                        embeddingVerified =
                            it[TicketTable.embeddingVerified],

                        embeddingAttempts =
                            it[TicketTable.embeddingAttempts]
                    )
                }
        }
    }


    /*
     * =========================================================
     * FIND TICKET BY ID
     * =========================================================
     */

    fun findById(
        ticketId: String
    ): Ticket? {

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.ticketId eq ticketId
                }
                .map {

                    Ticket(

                        ticketId =
                            it[TicketTable.ticketId],

                        created =
                            it[TicketTable.created],

                        closedAt =
                            it[TicketTable.closedAt],

                        state =
                            it[TicketTable.state],

                        category =
                            it[TicketTable.category],

                        urgency =
                            it[TicketTable.urgency],

                        customerName =
                            it[TicketTable.customerName],

                        customerPhone =
                            it[TicketTable.customerPhone],

                        messageCount =
                            it[TicketTable.messageCount],

                        title =
                            it[TicketTable.title],

                        description =
                            it[TicketTable.description],

                        resolution =
                            it[TicketTable.resolution],

                        conversation =
                            it[TicketTable.conversation],

                        embeddingVerified =
                            it[TicketTable.embeddingVerified],

                        embeddingAttempts =
                            it[TicketTable.embeddingAttempts]
                    )
                }
                .firstOrNull()
        }
    }
}