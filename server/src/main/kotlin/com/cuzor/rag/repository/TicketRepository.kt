package com.cuzor.rag.repository

import com.cuzor.rag.database.TicketTable
import com.cuzor.rag.models.Ticket
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll

object TicketRepository {

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
     
    fun exists(ticketId: String): Boolean {
        return transaction {
            TicketTable.selectAll().where { TicketTable.ticketId eq ticketId }.count() > 0
        }
    }
    fun count(): Long {

        return transaction {

            TicketTable.selectAll().count()

        }

    }
    fun keywordSearch(query: String): List<Ticket> {

        println("Searching database for: $query")

        return transaction {
            TicketTable.selectAll().map {
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

    fun findById(ticketId: String): Ticket? {

        return transaction {

            TicketTable
                .selectAll()
                .where { TicketTable.ticketId eq ticketId }
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

