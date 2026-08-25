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

                SchemaUtils.create(
                    TicketTable
                )

                exec(
                    """
        CREATE EXTENSION IF NOT EXISTS vector
        """.trimIndent()
                )

                exec(

                    """
        CREATE TABLE IF NOT EXISTS ticket_embeddings (
            ticket_id VARCHAR(100) PRIMARY KEY,
            embedding vector(1024)
        )
        """.trimIndent()
                )

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