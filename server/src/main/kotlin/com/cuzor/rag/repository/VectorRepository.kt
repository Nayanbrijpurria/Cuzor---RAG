package com.cuzor.rag.repository

import com.cuzor.rag.database.VectorTable
import com.cuzor.rag.models.EmbeddingVector
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object VectorRepository {

    fun save(vector: EmbeddingVector) {

        transaction {

            VectorTable.insert {

                it[id] = vector.ticketId

                it[VectorTable.embedding] =
                    vector.vector.joinToString(",")

            }

        }

    }

}