package com.cuzor.rag.repository

import com.cuzor.rag.models.EmbeddingVector
import org.jetbrains.exposed.sql.transactions.transaction

// This file is responsible for saving and searching embeddings in the PostgreSQL database using the pgvector extension.

object EmbeddingRepository {

    /**
     * Save or update an embedding.
     *
     * If the ticket already has an embedding,
     * the existing vector is replaced.
     */
    fun save(
        embedding: EmbeddingVector
    ) {

        transaction {

            val vectorString =
                embedding.vector.joinToString(
                    prefix = "[",
                    postfix = "]"
                )

            exec(
                """
                INSERT INTO ticket_embeddings (
                    ticket_id,
                    embedding
                )
                VALUES (
                    '${embedding.ticketId}',
                    '$vectorString'::vector
                )
                ON CONFLICT (ticket_id)
                DO UPDATE SET
                    embedding = EXCLUDED.embedding
                """.trimIndent()
            )
        }
    }



    /**
     * Check whether an embedding already exists.
     */
    fun exists(
        ticketId: String
    ): Boolean {

        return transaction {

            var exists = false

            exec(
                """
                SELECT 1
                FROM ticket_embeddings
                WHERE ticket_id = '$ticketId'
                LIMIT 1
                """.trimIndent()
            ) { resultSet ->

                exists = resultSet.next()
            }

            exists
        }
    }


    /**
     * Vector similarity search using PostgreSQL + pgvector.
     *
     * HNSW is used by PostgreSQL because the query:
     *
     *     ORDER BY embedding <=> queryVector
     *
     * matches the HNSW index.
     *
     * threshold:
     *     Minimum cosine similarity that we want
     *     to return to the application.
     *
     * limit:
     *     Maximum number of nearest-neighbor
     *     candidates PostgreSQL retrieves.
     */
    fun search(
        queryVector: FloatArray,
        threshold: Float = 0.5f,
        limit: Int = 20
    ): List<Pair<String, Float>> {

        return transaction {

            // ==========================================
            // SAFETY LIMIT
            // ==========================================
            //
            // Prevent an accidental request such as:
            //
            // limit = 100000
            //
            // For your 5,000–10,000 ticket system,
            // 100 is a reasonable initial maximum.
            //

            val safeLimit =
                limit.coerceIn(1, 100)


            val vectorString =
                queryVector.joinToString(
                    prefix = "[",
                    postfix = "]"
                )


            val results =
                mutableListOf<Pair<String, Float>>()


            // ==========================================
            // HNSW VECTOR SEARCH
            // ==========================================

            exec(
                """
                SELECT
                    ticket_id,

                    1 - (
                        embedding <=> '$vectorString'::vector
                    ) AS similarity

                FROM ticket_embeddings

                ORDER BY
                    embedding <=> '$vectorString'::vector

                LIMIT $safeLimit
                """.trimIndent()
            ) { resultSet ->

                while (resultSet.next()) {

                    val ticketId =
                        resultSet.getString(
                            "ticket_id"
                        )


                    val similarity =
                        resultSet.getFloat(
                            "similarity"
                        )


                    // ==================================
                    // CONFIDENCE FILTER
                    // ==================================

                    if (similarity >= threshold) {

                        results.add(
                            Pair(
                                ticketId,
                                similarity
                            )
                        )
                    }
                }
            }


            results
        }
    }
}