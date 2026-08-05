package com.cuzor.rag.repository

import com.cuzor.rag.database.VectorTable
import com.cuzor.rag.models.EmbeddingVector
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object EmbeddingRepository {

    fun save(

        embedding: EmbeddingVector

    ) {

        transaction {

            VectorTable.insert {

                it[id] = embedding.ticketId

                it[VectorTable.embedding] =
                    embedding.vector.joinToString(",")

            }

        }

    }

    fun findAll(): List<EmbeddingVector> {
        return transaction {
            VectorTable.selectAll().map { row ->
                EmbeddingVector(
                    ticketId = row[VectorTable.id].value,
                    vector = row[VectorTable.embedding]
                        .split(",")
                        .map { it.toFloat() }
                        .toFloatArray()
                )
            }
        }
    }

}