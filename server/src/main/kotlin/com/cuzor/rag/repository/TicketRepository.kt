package com.cuzor.rag.repository

import com.cuzor.rag.database.TicketTable
import com.cuzor.rag.models.EmbeddingStatus
import com.cuzor.rag.models.Ticket
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import com.cuzor.rag.models.EmbeddingHealth
import com.cuzor.rag.models.ProductAnalytics
import com.cuzor.rag.services.ProductExtractor
import com.cuzor.rag.models.UnknownProductTicket

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.isActive
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

                    it[product] = ticket.product
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
 * GET EMBEDDING HEALTH
 * =========================================================
 *
 * Returns the current embedding status of all tickets.
 *
 * 0 = PENDING
 * 1 = EMBEDDED
 * 2 = FAILED
 */
    fun getEmbeddingHealth(): EmbeddingHealth {

        return transaction {

            val total =
                TicketTable
                    .selectAll()
                    .count()

            val pending =
                TicketTable
                    .selectAll()
                    .where {
                        TicketTable.embeddingVerified eq
                                EmbeddingStatus.PENDING.code
                    }
                    .count()

            val embedded =
                TicketTable
                    .selectAll()
                    .where {
                        TicketTable.embeddingVerified eq
                                EmbeddingStatus.EMBEDDED.code
                    }
                    .count()

            val failed =
                TicketTable
                    .selectAll()
                    .where {
                        TicketTable.embeddingVerified eq
                                EmbeddingStatus.FAILED.code
                    }
                    .count()

            EmbeddingHealth(
                total = total,
                pending = pending,
                embedded = embedded,
                failed = failed
            )
        }
    }

    /*
 * =========================================================
 * FIND FAILED EMBEDDING TICKETS
 * =========================================================
 *
 * Returns tickets that reached:
 *
 * embedding_verified = 2
 *
 * These tickets have failed embedding twice and are
 * therefore excluded from the normal embedding Flow.
 */
    fun findFailedEmbeddingTickets(): List<Ticket> {

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.embeddingVerified eq
                            EmbeddingStatus.FAILED.code
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

                        product = it[TicketTable.product],

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


    //----------FLOW----------------

    // This function returns a Flow of pending tickets for embedding.
    // It continuously checks for pending tickets at specified intervals and emits them one by one.

    fun pendingEmbeddingFlow(
        batchSize: Int = 50,
        intervalMinutes: Long
    ): Flow<Ticket> = flow {

        while (currentCoroutineContext().isActive) {

            val pendingTickets =
                findPendingEmbeddingTickets(batchSize)

            if (pendingTickets.isEmpty()) {

                println("No pending tickets found.")

            } else {

                println(
                    "Flow found ${pendingTickets.size} pending ticket(s)."
                )

                pendingTickets.forEach { ticket ->

                    println(
                        "Flow emitting ticket: ${ticket.ticketId}"
                    )

                    emit(ticket)
                }
            }
            delay(intervalMinutes.minutes)
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

    /*
 * =========================================================
 * BACKFILL PRODUCT INFORMATION
 * =========================================================
 *
 * Used for existing tickets that were imported before
 * product extraction was added.
 *
 * This ONLY updates the product column.
 * It does NOT touch embeddings.
 */

    fun backfillProducts(): Map<String, Long> {

        return transaction {

            val tickets =
                TicketTable
                    .selectAll()
                    .map {
                        Triple(
                            it[TicketTable.ticketId],
                            it[TicketTable.conversation],
                            it[TicketTable.product]
                        )
                    }

            var processed = 0L
            var miniUps = 0L
            var pulseGaN = 0L
            var unknown = 0L

            tickets.forEach { (ticketId, conversation, existingProduct) ->

                /*
                 * If product is already known,
                 * don't process it again.
                 */
                if (!existingProduct.isNullOrBlank()) {
                    processed++
                    return@forEach
                }

                val product =
                    ProductExtractor.extract(conversation)

                if (product != null) {

                    TicketTable.update(
                        where = {
                            TicketTable.ticketId eq ticketId
                        }
                    ) {
                        it[TicketTable.product] = product
                    }

                    when (product) {

                        "Mini UPS" -> {
                            miniUps++
                        }

                        "Pulse GaN Charger" -> {
                            pulseGaN++
                        }
                    }

                } else {

                    unknown++
                }

                processed++

                if (processed % 500 == 0L) {

                    println(
                        "Product backfill progress: " +
                                "$processed / ${tickets.size}"
                    )
                }
            }

            println("===================================")
            println("PRODUCT BACKFILL COMPLETED")
            println("===================================")
            println("Processed: $processed")
            println("Mini UPS: $miniUps")
            println("Pulse GaN Charger: $pulseGaN")
            println("Unknown: $unknown")
            println("===================================")

            mapOf(
                "processed" to processed,
                "miniUps" to miniUps,
                "pulseGaNCharger" to pulseGaN,
                "unknown" to unknown
            )
        }
    }
    //----------------
    /*
 * =========================================================
 * GET UNKNOWN PRODUCT TICKETS
 * =========================================================
 *
 * Returns tickets where product could not be identified.
 *
 * Used to inspect the actual conversation format and
 * improve ProductExtractor using deterministic rules.
 */

    fun getUnknownProductTickets(
        limit: Int = 20
    ): List<UnknownProductTicket> {

        val safeLimit =
            limit.coerceIn(1, 100)

        return transaction {

            TicketTable
                .selectAll()
                .where {
                    TicketTable.product.isNull()
                }
                .limit(safeLimit)
                .map {

                    UnknownProductTicket(

                        ticketId =
                            it[TicketTable.ticketId],

                        title =
                            it[TicketTable.title],

                        conversation =
                            it[TicketTable.conversation]
                    )
                }
        }
    }

    //-------------

    fun getProductAnalytics(): List<ProductAnalytics> {

        return transaction {

            exec(
                """
            SELECT
                product,
                COUNT(*) AS query_count
            FROM tickets
            WHERE product IS NOT NULL
              AND TRIM(product) <> ''
            GROUP BY product
            ORDER BY query_count DESC
            """.trimIndent()
            ) { resultSet ->

                val results = mutableListOf<ProductAnalytics>()

                while (resultSet.next()) {

                    results.add(
                        ProductAnalytics(
                            product = resultSet.getString("product"),
                            queryCount = resultSet.getLong("query_count")
                        )
                    )
                }

                results
            } ?: emptyList()
        }
    }
}