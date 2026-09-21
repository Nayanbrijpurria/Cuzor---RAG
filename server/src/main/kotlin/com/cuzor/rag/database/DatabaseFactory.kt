package com.cuzor.rag.database

import com.cuzor.rag.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(config: AppConfig) {

        val hikariConfig = HikariConfig().apply {

            jdbcUrl =
                "jdbc:postgresql://${config.host}:${config.port}/${config.database}"

            driverClassName = "org.postgresql.Driver"

            username = config.username

            password = config.password

            maximumPoolSize = 10

            isAutoCommit = false

            transactionIsolation =
                "TRANSACTION_REPEATABLE_READ"
        }

        try {

            val dataSource =
                HikariDataSource(hikariConfig)

            Database.connect(dataSource)

            transaction {

                // Create the tickets table if it does not exist
                SchemaUtils.create(
                    TicketTable
                )

                /*
                 * Add embedding verification status.
                 *
                 * 0 = embedding is pending
                 * 1 = embedding has been successfully generated
                 *
                 * IF NOT EXISTS makes this safe to run
                 * when the column already exists.
                 */
                exec(
                    """
                    ALTER TABLE tickets
                    ADD COLUMN IF NOT EXISTS embedding_verified
                    INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                // Create pgvector extension
                exec(
                    """
                    CREATE EXTENSION IF NOT EXISTS vector
                    """.trimIndent()
                )

                // Create vector table
                exec(
                    """
                    CREATE TABLE IF NOT EXISTS ticket_embeddings (
                        ticket_id VARCHAR(100) PRIMARY KEY,
                        embedding vector(1024)
                    )
                    """.trimIndent()
                )

                // Create HNSW vector index
                exec(
                    """
                    CREATE INDEX IF NOT EXISTS ticket_embeddings_vector_idx
                    ON ticket_embeddings
                    USING hnsw (embedding vector_cosine_ops)
                    """.trimIndent()
                )
            }

            println("✅ PostgreSQL Connected Successfully")

        } catch (exception: Exception) {

            println(
                "⚠️ PostgreSQL connection unavailable: ${exception.message}"
            )
        }
    }
}