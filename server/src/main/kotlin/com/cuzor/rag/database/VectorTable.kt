package com.cuzor.rag.database

import org.jetbrains.exposed.dao.id.IdTable

object VectorTable : IdTable<String>("ticket_embeddings") {

    override val id = varchar("ticket_id", 100).entityId()

    val embedding = text("embedding")

    override val primaryKey = PrimaryKey(id)

}