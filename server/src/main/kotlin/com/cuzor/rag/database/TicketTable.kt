package com.cuzor.rag.database

import org.jetbrains.exposed.sql.Table

object TicketTable : Table("tickets") {

    val ticketId = varchar("ticket_id", 50)

    val created = varchar("created", 100)

    val closedAt = varchar("closed_at", 100)

    val state = varchar("state", 30)

    val category = varchar("category", 30)

    val urgency = integer("urgency")

    val customerName = varchar("customer_name", 150)

    val customerPhone = varchar("customer_phone", 30)

    val messageCount = integer("message_count")

    val title = varchar("title", 300)

    val description = text("description")

    val resolution = text("resolution")

    val conversation = text("conversation")

    // 0 = embedding pending
    // 1 = embedding successfully generated
    val embeddingVerified = integer("embedding_verified")

    override val primaryKey = PrimaryKey(ticketId)
}