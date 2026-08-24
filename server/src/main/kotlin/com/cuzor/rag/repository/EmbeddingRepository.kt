package com.cuzor.rag.repository

import com.cuzor.rag.models.EmbeddingVector
import org.jetbrains.exposed.sql.transactions.transaction

object EmbeddingRepository {

    fun save(embedding: EmbeddingVector) {

        transaction {

            val vectorString =
                embedding.vector.joinToString(
                    prefix = "[",
                    postfix = "]"
                )

            exec(
                """
                INSERT INTO ticket_embeddings (ticket_id, embedding)
                VALUES ('${embedding.ticketId}', '$vectorString'::vector)
                ON CONFLICT (ticket_id)
                DO UPDATE SET embedding = EXCLUDED.embedding
                """.trimIndent()
            )
        }
    }

    fun search(
        queryVector: FloatArray,
        threshold: Float
    ): List<Pair<String, Float>> {

        return transaction {

            val vectorString =
                queryVector.joinToString(
                    prefix = "[",
                    postfix = "]"
                )

            val maxDistance = 1 - threshold

            val results =
                mutableListOf<Pair<String, Float>>()

            exec(
                """
                SELECT
                    ticket_id,
                    1 - (embedding <=> '$vectorString'::vector)
                        AS similarity
                FROM ticket_embeddings
                WHERE
                    embedding <=> '$vectorString'::vector
                    < $maxDistance
                ORDER BY embedding <=> '$vectorString'::vector
                """.trimIndent()
            ) { resultSet ->

                while (resultSet.next()) {

                    results.add(
                        Pair(
                            resultSet.getString("ticket_id"),
                            resultSet.getFloat("similarity")
                        )
                    )
                }
            }

            results
        }
    }
}